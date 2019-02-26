import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

public class Solver {
	
	Field field;
	Truck truck;
	static final double ACCEPTED_ERROR = 1E-6;
	
	public Solver(Field field, Truck truck) {
		this.field = field;
		this.truck = truck;
	}
	
	/*
	 * Build a chain of peaks from "from" coordinates by using a 
	 * nearest neighbor strategy. 
	 */
	
	private ArrayList <Movement> getChainOfPeaks(Coordinates from, double quantity) throws IOException {
		Truck newTruck = new Truck(quantity, truck.minimumMove, field.getTheNearestPeak(from));
		newTruck.move(newTruck.currentPosition); // just to add at least a movement
		while(newTruck.currentPosition != null) {
			if(field.getQuantity(newTruck.currentPosition) >= newTruck.capacity)
				break;
			Coordinates nextPeak = field.getTheNearestPeakDifferentFromThisOne(newTruck.currentPosition, newTruck.currentPosition);
			if(nextPeak == null)
				break;
			newTruck.move(nextPeak, field.getQuantity(newTruck.currentPosition));
			field.update(newTruck.getLastMovement());
		}
		field.decrement(newTruck.currentPosition, newTruck.capacity);
		return newTruck.path;
	}
	
	/*
	 * Get all the chains of peaks.
	 */
	
	private ArrayList <ArrayList <Movement>> getAllChainsOfPeaks() throws IOException {
		ArrayList <ArrayList <Movement>> chainsOfPeaks = new ArrayList <> ();
		Coordinates nextPeak = field.getAPeak();
		while(nextPeak != null) {
			ArrayList <Movement> chain = getChainOfPeaks(nextPeak, truck.capacity);
			chainsOfPeaks.add(chain);
			nextPeak = field.getTheNearestPeak(chain.get(chain.size() - 1).to);
		}
		return chainsOfPeaks;
	}
	
	/*
	 * Build a chain of holes from "from" coordinates by using a 
	 * nearest neighbor strategy. 
	 */
	
	private ArrayList <Movement> getChainOfHoles(Coordinates from, double quantity) throws IOException {
		Truck newTruck = new Truck(quantity, truck.minimumMove, field.getTheNearestHole(from));
		newTruck.move(newTruck.currentPosition); // just to add at least a movement
		field.increment(newTruck.currentPosition, newTruck.capacity);
		while(newTruck.currentPosition != null) {
			if(field.getQuantity(newTruck.currentPosition) <= 0)
				break;
			Coordinates nextHole = field.getTheNearestHole(newTruck.currentPosition);
			if(nextHole == null)
				break;
			newTruck.move(nextHole, field.getQuantity(newTruck.currentPosition));
			field.update(newTruck.getLastMovement());
		}
		return newTruck.path;
	}
	
	/*
	 * Get all the chains of holes.
	 */
	
	private ArrayList <ArrayList <Movement>> getAllChainsOfHoles() throws IOException {
		ArrayList <ArrayList <Movement>> chainsOfHoles = new ArrayList <> ();
		Coordinates nextHole = field.getAnHole();
		while(nextHole != null) {
			ArrayList <Movement> chain = getChainOfHoles(nextHole, truck.capacity);
			chainsOfHoles.add(chain);
			nextHole = field.getTheNearestHole(chain.get(chain.size() - 1).to);
		}
		return chainsOfHoles;
	}
	
	/*
	 * 
	 */
	
	private void fixField() throws IOException {
		double terrainToMove = field.terrainToMove();
		double remainder = terrainToMove - Math.floor(terrainToMove / truck.capacity) * truck.capacity;
		if(remainder < field.MAX_ERROR)
			return;
		ArrayList <Movement> chainOfPeaks = getChainOfPeaks(field.getAPeak(), remainder);
		ArrayList <Movement> chainOfHoles = getChainOfHoles(chainOfPeaks.get(chainOfPeaks.size() - 1).to, remainder);
		truck.move(chainOfPeaks);
		truck.move(chainOfHoles.get(0).from, remainder);
		truck.move(chainOfHoles);
	}
	
	/*
	 * 
	 */
	
	private static double[][] buildMatrixOfDistances(ArrayList <ArrayList <Movement>> chainsOfPeaks, ArrayList <ArrayList <Movement>> chainsOfHoles) {
		double[][] matrix = new double[chainsOfPeaks.size()][chainsOfHoles.size()];
		for(int i = 0; i < chainsOfPeaks.size(); i ++) 
			for(int j = 0; j < chainsOfHoles.size(); j ++)
				matrix[i][j] = chainsOfPeaks.get(i).get(chainsOfPeaks.get(i).size() - 1).to.distance(chainsOfHoles.get(j).get(0).from);
		return matrix;
	}
	
	/*
	 * 
	 */
	
	public static double getAngle(Movement m1, Movement m2) {
		return new Vector2D(m1).getAngle(new Vector2D(m2));
	}
	
	/*
	 * 
	 */
	
	public static boolean isOk(double angle) {
		return angle <= Math.PI / 2 + ACCEPTED_ERROR;
	}
	
	/*
	 * 
	 */
	
	private Coordinates singleStopover(Movement m1, Movement m2) {
		double angle = getAngle(m1, m2);
		if(angle > 0.75 * Math.PI)
			return null;
		Vector2D v = new Vector2D(m2);
		for(Vector2D dir : v.getVectorsByAngle(angle - Math.PI / 2, truck.minimumMove)) {
			Coordinates s = new Coordinates(m1.to, dir);
			
			if(!isOk(getAngle(m1, new Movement(m1.to, s)))) // turn on the other side
				continue;
			if(!isOk(getAngle(new Movement(m1.to, s), new Movement(s, m2.to)))) // truck.minimumMove too large
				continue;
			if(!field.contains(s))
				continue;
			
			return s;
		}
		return null;
	}
	
	/*
	 * 
	 */
	
	private ArrayList <Coordinates> twoStopovers(Movement m1, Movement m2) {
		double angle = getAngle(m1, m2);
		Vector2D v = new Vector2D(m2);
		for(Vector2D dir1 : v.getVectorsByAngle(angle - Math.PI / 2, truck.minimumMove)) {
			Coordinates s1 = new Coordinates(m1.to, dir1);
			
			if(!isOk(getAngle(m1, new Movement(m1.to, s1)))) // turn on the other side
				continue;
			if(!field.contains(s1))
				continue;

			Coordinates s2 = new Coordinates(s1, v.setLengthTo(truck.minimumMove));
			
			if(!isOk(getAngle(new Movement(s1, s2), new Movement(s2, m2.to)))) // truck.minimumMove too large
				continue;
			if(!field.contains(s2))
				continue;
			
			ArrayList <Coordinates> stopovers = new ArrayList <> ();
			stopovers.add(s1);
			stopovers.add(s2);
			return stopovers;
		}
		return null;
	}
	
	/*
	 * 
	 */
	
	private void fixPath() throws IOException {
		for(int i = 0; i < truck.path.size(); i ++)
			if(truck.getMovement(i).from.equals(truck.getMovement(i).to))
					truck.removeMovement(i --);
		
		for(int i = 1; i < truck.path.size(); i ++) {
			double angle = getAngle(truck.getMovement(i - 1), truck.getMovement(i));
			
			if(isOk(angle))
				continue;
			
			Coordinates stopover = singleStopover(truck.getMovement(i - 1), truck.getMovement(i));
			if(stopover != null) {
				truck.addStopoverAfter(stopover, i - 1);
				continue;
			}
			
			ArrayList <Coordinates> stopovers = twoStopovers(truck.getMovement(i - 1), truck.getMovement(i));
			if(stopovers == null) 
				throw new IOException("Unable to fix");
			truck.addStopoversAfter(stopovers, i - 1);
		}
	}
	
	/*
	 * 
	 */
	
	private static int getTheIndexOfTheNearest(Coordinates from, ArrayList <ArrayList <Movement>> chains, boolean[] done) {
		int nearest = -1;
		for(int i = 0; i < chains.size(); i ++)
			if(!done[i]) {
				if(nearest == -1)
					nearest = i;
				else if(from.distance(chains.get(i).get(0).from) < from.distance(chains.get(nearest).get(0).from))
					nearest = i;
			}
		return nearest;
	}
	
	/*
	 * 
	 */
	
	public ArrayList <Movement> solve() throws IOException {
		fixField();
		ArrayList <ArrayList <Movement>> chainsOfPeaks = getAllChainsOfPeaks();
		ArrayList <ArrayList <Movement>> chainsOfHoles = getAllChainsOfHoles();
		int[] assignment = new HungarianAlgorithm(buildMatrixOfDistances(chainsOfPeaks, chainsOfHoles)).execute();
		boolean[] done = new boolean[chainsOfPeaks.size()];
		while(true) {
			int nearest = getTheIndexOfTheNearest(truck.currentPosition == null ? field.getAPeak() : truck.currentPosition, chainsOfPeaks, done);
			if(nearest == -1)
				break;
			done[nearest] = true;
			ArrayList <Movement> chainOfPeaks = chainsOfPeaks.get(nearest);
			ArrayList <Movement> chainOfHoles = chainsOfHoles.get(assignment[nearest]);
			truck.move(chainOfPeaks);
			truck.move(chainOfHoles.get(0).from, truck.capacity);
			truck.move(chainOfHoles);
		}
		fixPath();
		return truck.path;
	}
}

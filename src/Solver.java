import java.io.IOException;
import java.util.ArrayList;

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
	
	private Path getChainOfPeaks(Coordinates from, double quantity) throws IOException {
		Truck newTruck = new Truck(quantity, truck.minimumMove, field.getTheNearestPeak(from), 0.0);
		newTruck.move(newTruck.getCurrentPosition()); // just to add at least a movement
		while(true) {
			if(field.getQuantity(newTruck.getCurrentPosition()) >= newTruck.capacity)
				break;
			Coordinates nextPeak = field.getTheNearestPeakDifferentFromThisOne(newTruck.getCurrentPosition(), newTruck.getCurrentPosition());
			if(nextPeak == null)
				break;
			newTruck.move(nextPeak, field.getQuantity(newTruck.getCurrentPosition()));
			field.update(newTruck.getLastMovement());
		}
		field.decrement(newTruck.getCurrentPosition(), newTruck.capacity);
		return newTruck.path;
	}
	
	/*
	 * Get all the chains of peaks.
	 */
	
	private ArrayList <Path> getAllChainsOfPeaks(Coordinates from) throws IOException {
		ArrayList <Path> chainsOfPeaks = new ArrayList <> ();
		Coordinates nextPeak = field.getTheNearestHole(from);
		while(nextPeak != null) {
			Path chain = getChainOfPeaks(nextPeak, truck.capacity);
			chainsOfPeaks.add(chain);
			nextPeak = field.getTheNearestPeak(chain.getLastCoordinates());
		}
		return chainsOfPeaks;
	}
	
	/*
	 * Build a chain of holes from "from" coordinates by using a 
	 * nearest neighbor strategy. 
	 */
	
	private Path getChainOfHoles(Coordinates from, double quantity) throws IOException {
		/*
		 * PAY ATTENTION: the truck has to bring in the first cell "quantity" units of terrain 
		 * to fill the chain of holes.
		 */
		
		Truck newTruck = new Truck(quantity, truck.minimumMove, field.getTheNearestHole(from), quantity);
		field.increment(newTruck.getCurrentPosition(), newTruck.capacity);
		while(true) {
			if(field.getQuantity(newTruck.getCurrentPosition()) <= 0)
				break;
			Coordinates nextHole = field.getTheNearestHole(newTruck.getCurrentPosition());
			if(nextHole == null)
				break;
			newTruck.move(nextHole, field.getQuantity(newTruck.getCurrentPosition()));
			field.update(newTruck.getLastMovement());
		}
		return newTruck.path;
	}
	
	/*
	 * Get all the chains of holes.
	 */
	
	private ArrayList <Path> getAllChainsOfHoles(Coordinates from) throws IOException {
		ArrayList <Path> chainsOfHoles = new ArrayList <> ();
		Coordinates nextHole = field.getTheNearestHole(from);
		while(nextHole != null) {
			Path chain = getChainOfHoles(nextHole, truck.capacity);
			chainsOfHoles.add(chain);
			nextHole = field.getTheNearestHole(chain.getLastCoordinates());
		}
		return chainsOfHoles;
	}
	
	private void fixField() throws IOException {
		double terrainToMove = field.terrainToMove();
		double remainder = terrainToMove - Math.floor(terrainToMove / truck.capacity) * truck.capacity;
		if(remainder < field.MAX_ERROR)
			return;
		Path chainOfPeaks = getChainOfPeaks(truck.getCurrentPosition(), remainder);
		Path chainOfHoles = getChainOfHoles(chainOfPeaks.getLastCoordinates(), remainder);
		truck.move(chainOfPeaks);
		truck.move(chainOfHoles);
	}
	
	private static double[][] buildMatrixOfDistances(ArrayList <Path> chainsOfPeaks, ArrayList <Path> chainsOfHoles) {
		double[][] matrix = new double[chainsOfPeaks.size()][chainsOfHoles.size()];
		for(int i = 0; i < chainsOfPeaks.size(); i ++) 
			for(int j = 0; j < chainsOfHoles.size(); j ++)
				matrix[i][j] = chainsOfPeaks.get(i).getLastCoordinates().distance(chainsOfHoles.get(j).getFirstCoordinates());
		return matrix;
	}
	
	public static double getAngle(Coordinates c1, Coordinates c2, Coordinates c3) {
		return c2.subtract(c1).getAngle(c3.subtract(c2));
	}
	
	public static boolean isOk(double angle) {
		return angle <= Math.PI / 2 + ACCEPTED_ERROR;
	}
	
	private Coordinates singleStopover(Coordinates c1, Coordinates c2, Coordinates c3) {
		double angle = getAngle(c1, c2, c3);
		if(angle > 0.75 * Math.PI)
			return null;
		Vector2D v = c3.subtract(c2);
		for(Vector2D dir : v.getVectorsByAngle(angle - Math.PI / 2, truck.minimumMove)) {
			Coordinates s = new Coordinates(c2, dir);
			
			if(!isOk(getAngle(c1, c2, s))) // turn on the other side
				continue;
			if(!isOk(getAngle(c2, s, c3))) // truck.minimumMove too large
				continue;
			if(!field.contains(s))
				continue;
			
			return s;
		}
		return null;
	}
	
	private ArrayList <Coordinates> twoStopovers(Coordinates c1, Coordinates c2, Coordinates c3) {
		double angle = getAngle(c1, c2, c3);
		Vector2D v = c3.subtract(c2);
		for(Vector2D dir1 : v.getVectorsByAngle(angle - Math.PI / 2, truck.minimumMove)) {
			Coordinates s1 = new Coordinates(c2, dir1);
			
			if(!isOk(getAngle(c1, c2, s1))) // turn on the other side
				continue;
			if(!field.contains(s1))
				continue;

			Coordinates s2 = new Coordinates(s1, v.setLengthTo(truck.minimumMove));
			
			if(!isOk(getAngle(s1, s2, c3))) // truck.minimumMove too large
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
	
	private void fixPath() throws IOException {
		for(int i = 2; i < truck.path.length(); i ++) {
			double angle = getAngle(truck.path.getCoordinates(i - 2), truck.path.getCoordinates(i - 1), truck.path.getCoordinates(i));
			
			if(isOk(angle))
				continue;
			
			Coordinates stopover = singleStopover(truck.path.getCoordinates(i - 2), truck.path.getCoordinates(i - 1), truck.path.getCoordinates(i));
			if(stopover != null) {
				truck.path.rerouteOne(i - 1, stopover);
				continue;
			}
			
			ArrayList <Coordinates> stopovers = twoStopovers(truck.path.getCoordinates(i - 2), truck.path.getCoordinates(i - 1), truck.path.getCoordinates(i));
			if(stopovers == null) 
				throw new IOException("Unable to fix");
			truck.path.rerouteTwo(i - 1, stopovers.get(0), stopovers.get(1));
		}
	}
	
	private static int getTheIndexOfTheNearest(Coordinates from, ArrayList <Path> chains, boolean[] done) {
		int nearest = -1;
		for(int i = 0; i < chains.size(); i ++)
			if(!done[i]) {
				if(nearest == -1)
					nearest = i;
				else if(from.distance(chains.get(i).getFirstCoordinates()) < from.distance(chains.get(nearest).getFirstCoordinates()))
					nearest = i;
			}
		return nearest;
	}
	
	public Path solve() throws IOException {
		fixField();
		ArrayList <Path> chainsOfPeaks = getAllChainsOfPeaks(truck.getCurrentPosition());
		ArrayList <Path> chainsOfHoles = getAllChainsOfHoles(truck.getCurrentPosition());
		int[] assignment = new HungarianAlgorithm(buildMatrixOfDistances(chainsOfPeaks, chainsOfHoles)).execute();
		boolean[] done = new boolean[chainsOfPeaks.size()];
		while(true) {
			int nearest = getTheIndexOfTheNearest(truck.getCurrentPosition(), chainsOfPeaks, done);
			if(nearest == -1)
				break;
			done[nearest] = true;
			Path chainOfPeaks = chainsOfPeaks.get(nearest);
			Path chainOfHoles = chainsOfHoles.get(assignment[nearest]);
			truck.move(chainOfPeaks);
			truck.move(chainOfHoles);
		}
		fixPath();
		return truck.path;
	}
}

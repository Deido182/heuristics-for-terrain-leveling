import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

public class Solver {
	
	/*
	 * 
	 */
	
	public static void addAndUpdate(Coordinates from, Coordinates to, double q, ArrayList <Movement> path, Field field) {
		Movement m = new Movement(from, to, q);
		path.add(m);
		field.update(m);
	}
	
	/*
	 * Build a chain of peaks from "from" coordinates by using a 
	 * nearest neighbor strategy. 
	 */
	
	public static ArrayList <Movement> getChainOfPeaks(Coordinates from, Field field, double capacity) {
		Coordinates currentPeak = field.getTheNearestPeak(from);
		Coordinates nextPeak = null;
		ArrayList <Movement> chainOfPeaks = new ArrayList <> ();
		addAndUpdate(currentPeak, currentPeak, 0.0, chainOfPeaks, field);
		while(currentPeak != null) {
			if(field.getQuantity(currentPeak) >= capacity)
				break;
			double q = field.getQuantity(currentPeak);
			field.decrement(currentPeak, q);
			nextPeak = field.getTheNearestPeak(currentPeak);
			field.increment(currentPeak, q);
			if(nextPeak == null)
				break;
			addAndUpdate(currentPeak, nextPeak, field.getQuantity(currentPeak), chainOfPeaks, field);
			currentPeak = nextPeak;
		}
		field.decrement(currentPeak, capacity);
		return chainOfPeaks;
	}
	
	/*
	 * Get all the chains of peaks.
	 */
	
	public static ArrayList <ArrayList <Movement>> getAllChainsOfPeaks(Field field, double capacity) {
		ArrayList <ArrayList <Movement>> chainsOfPeaks = new ArrayList <> ();
		Coordinates nextPeak = field.getAPeak();
		while(nextPeak != null) {
			ArrayList <Movement> chain = getChainOfPeaks(nextPeak, field, capacity);
			chainsOfPeaks.add(chain);
			nextPeak = field.getTheNearestPeak(chain.get(chain.size() - 1).to);
		}
		return chainsOfPeaks;
	}
	
	/*
	 * Build a chain of holes from "from" coordinates by using a 
	 * nearest neighbor strategy. 
	 */
	
	public static ArrayList <Movement> getChainOfHoles(Coordinates from, Field field, double capacity) {
		Coordinates currentHole = field.getTheNearestHole(from);
		Coordinates nextHole = null;
		ArrayList <Movement> chainOfHoles = new ArrayList <> ();
		addAndUpdate(currentHole, currentHole, 0.0, chainOfHoles, field);
		field.increment(currentHole, capacity);
		while(currentHole != null) {
			if(field.getQuantity(currentHole) <= 0)
				break;
			nextHole = field.getTheNearestHole(currentHole);
			if(nextHole == null)
				break;
			addAndUpdate(currentHole, nextHole, field.getQuantity(currentHole), chainOfHoles, field);
			currentHole = nextHole;
		}
		return chainOfHoles;
	}
	
	/*
	 * Get all the chains of holes.
	 */
	
	public static ArrayList <ArrayList <Movement>> getAllChainsOfHoles(Field field, double capacity) {
		ArrayList <ArrayList <Movement>> chainsOfHoles = new ArrayList <> ();
		Coordinates nextHole = field.getAnHole();
		while(nextHole != null) {
			ArrayList <Movement> chain = getChainOfHoles(nextHole, field, capacity);
			chainsOfHoles.add(chain);
			nextHole = field.getTheNearestHole(chain.get(chain.size() - 1).to);
		}
		return chainsOfHoles;
	}
	
	/*
	 * 
	 */
	
	public static ArrayList <Movement> fixField(Field field, double capacity) {
		ArrayList <Movement> path = new ArrayList <> ();
		double terrainToMove = field.terrainToMove();
		double remainder = terrainToMove - Math.floor(terrainToMove / capacity) * capacity;
		if(remainder < field.MAX_ERROR)
			return path;
		ArrayList <Movement> chainOfPeaks = getChainOfPeaks(field.getAPeak(), field, remainder);
		ArrayList <Movement> chainOfHoles = getChainOfHoles(chainOfPeaks.get(chainOfPeaks.size() - 1).to, field, remainder);
		path.addAll(chainOfPeaks);
		path.add(new Movement(chainOfPeaks.get(chainOfPeaks.size() - 1).to, chainOfHoles.get(0).from, remainder));
		path.addAll(chainOfHoles);
		return path;
	}
	
	/*
	 * 
	 */
	
	public static double[][] buildMatrixOfDistances(ArrayList <ArrayList <Movement>> chainsOfPeaks, ArrayList <ArrayList <Movement>> chainsOfHoles) {
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
		Coordinates v1 = new Coordinates(m1.to.x - m1.from.x, m1.to.y - m1.from.y);
		Coordinates v2 = new Coordinates(m2.to.x - m2.from.x, m2.to.y - m2.from.y);
		
		double scalarProduct = v1.x * v2.x + v1.y * v2.y;
		double normV1 = m1.distance();
		double normV2 = m2.distance();
		
		double cos = scalarProduct / (normV1 * normV2);
		
		/*
		 * The approximation of floating point representation could make it 
		 * possible.
		 */
		
		if(cos < -1.0)
			cos = -1.0;
		if(cos > 1.0)
			cos = 1.0;
		
		return Math.acos(cos);
	}
	
	/*
	 * 
	 */
	
	public static double ACCEPTED_ERROR = 1E-6;
	
	public static boolean isOk(double angle) {
		return angle <= Math.PI / 2 + ACCEPTED_ERROR;
	}
	
	/*
	 * 
	 */
	
	public static double euclideanNorm(Coordinates c) {
		return Math.sqrt(c.x * c.x + c.y * c.y);
	}
	
	/*
	 * 
	 */
	
	public static ArrayList <Coordinates> getVectorsByAngle(Coordinates v, double alpha, double length) {
		Coordinates v1 = new Coordinates(v.x * Math.cos(alpha) + v.y * Math.sin(alpha), -v.x * Math.sin(alpha) + v.y * Math.cos(alpha));
		Coordinates v2 = new Coordinates(v.x * Math.cos(-alpha) + v.y * Math.sin(-alpha), -v.x * Math.sin(-alpha) + v.y * Math.cos(-alpha));
		
		/*
		 * "Normalize"
		 */
		
		double norm = euclideanNorm(v1);
		v1.x /= norm / length;
		v1.y /= norm / length;
		v2.x /= norm / length;
		v2.y /= norm / length;
		
		ArrayList <Coordinates> vectors = new ArrayList <> ();
		vectors.add(v1);
		if(!v1.equals(v2))
			vectors.add(v2);
		
		return vectors;
	}
	
	/*
	 * 
	 */
	

	public static double MOVE = 1.5;
	
	public static Coordinates singleStopover(Movement m1, Movement m2, Field field) {
		double angle = getAngle(m1, m2);
		if(angle > 0.75 * Math.PI)
			return null;
		Coordinates v = new Coordinates(m2.to.x - m1.to.x, m2.to.y - m1.to.y);
		for(Coordinates c : getVectorsByAngle(v, angle - Math.PI / 2, MOVE)) {
			Coordinates s = new Coordinates(c.x + m1.to.x, c.y + m1.to.y);
			
			if(!isOk(getAngle(m1, new Movement(m1.to, s)))) // turn on the other side
				continue;
			if(!isOk(getAngle(new Movement(m1.to, s), new Movement(s, m2.to)))) // MOVE too large
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
	
	public static ArrayList <Coordinates> twoStopovers(Movement m1, Movement m2, Field field) {
		double angle = getAngle(m1, m2);
		Coordinates v = new Coordinates(m2.to.x - m1.to.x, m2.to.y - m1.to.y);
		for(Coordinates c1 : getVectorsByAngle(v, angle - Math.PI / 2, MOVE)) {
			Coordinates s1 = new Coordinates(c1.x + m1.to.x, c1.y + m1.to.y);
			
			if(!isOk(getAngle(m1, new Movement(m1.to, s1)))) // turn on the other side
				continue;
			if(!field.contains(s1))
				continue;

			double normV = euclideanNorm(v);
			
			v.x /= normV / MOVE;
			v.y /= normV / MOVE;
			Coordinates s2 = new Coordinates(v.x + s1.x, v.y + s1.y);
			
			if(!isOk(getAngle(new Movement(s1, s2), new Movement(s2, m2.to)))) // MOVE too large
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
	
	public static void fixPath(ArrayList <Movement> path, Field field) throws IOException {
		for(int i = 1; i < path.size(); i ++) {
			double angle = getAngle(path.get(i - 1), path.get(i));
			
			if(isOk(angle))
				continue;
			
			Movement m1 = path.get(i - 1);
			Movement m2 = path.get(i);
			
			Coordinates stopover = singleStopover(m1, m2, field);
			if(stopover != null) {
				path.remove(i);
				path.add(i, new Movement(m1.to, stopover, m2.quantity));
				path.add(i + 1, new Movement(stopover, m2.to, m2.quantity));
				continue;
			}
			
			ArrayList <Coordinates> stopovers = twoStopovers(m1, m2, field);
			if(stopovers == null) 
				throw new IOException("Unable to fix");
			path.remove(i);
			path.add(i, new Movement(m1.to, stopovers.get(0), m2.quantity));
			path.add(i + 1, new Movement(stopovers.get(0), stopovers.get(1), m2.quantity));
			path.add(i + 2, new Movement(stopovers.get(1), m2.to, m2.quantity));
		}
	}
	
	/*
	 * 
	 */
	
	public static int getTheIndexOfTheNearest(Coordinates from, ArrayList <ArrayList <Movement>> chains, boolean[] done) {
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
	
	public static ArrayList <Movement> solve(Field field, double capacity) throws IOException {
		ArrayList <Movement> path = fixField(field, capacity);
		Coordinates currentCoor = path.size() > 0 ? path.get(path.size() - 1).to : field.getAPeak();
		ArrayList <ArrayList <Movement>> chainsOfPeaks = getAllChainsOfPeaks(field, capacity);
		ArrayList <ArrayList <Movement>> chainsOfHoles = getAllChainsOfHoles(field, capacity);
		int[] assignment = new HungarianAlgorithm(buildMatrixOfDistances(chainsOfPeaks, chainsOfHoles)).execute();
		boolean[] done = new boolean[chainsOfPeaks.size()];
		while(true) {
			int nearest = getTheIndexOfTheNearest(currentCoor, chainsOfPeaks, done);
			if(nearest == -1)
				break;
			done[nearest] = true;
			ArrayList <Movement> chainOfPeaks = chainsOfPeaks.get(nearest);
			ArrayList <Movement> chainOfHoles = chainsOfHoles.get(assignment[nearest]);
			path.add(new Movement(currentCoor, chainOfPeaks.get(0).from, 0.0));
			path.addAll(chainOfPeaks);
			path.add(new Movement(chainOfPeaks.get(chainOfPeaks.size() - 1).to, chainOfHoles.get(0).from, capacity));
			path.addAll(chainOfHoles);
			currentCoor = path.get(path.size() - 1).to;
		}
		for(int i = 0; i < path.size(); i ++)
			if(path.get(i).from.equals(path.get(i).to))
					path.remove(i --);
		fixPath(path, field);
		return path;
	}
}

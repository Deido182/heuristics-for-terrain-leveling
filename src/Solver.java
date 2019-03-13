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
	
	/**
	 * Creates a path of peaks to collect the required quantity of terrain.
	 * 
	 * @param from the coordinates from which search the first peak.
	 * @param quantity the quantity of terrain we want to leave at the end of the chain.
	 * @return the path.
	 */
	
	private Path getChainOfPeaks(Coordinates from, long quantity) {
		Truck newTruck = new Truck(quantity, field.getTheNearestPeak(from), 0);
		newTruck.move(newTruck.getCurrentPosition()); // just to add at least a movement
		while(field.getQuantity(newTruck.getCurrentPosition()) < newTruck.capacity) {
			Coordinates nextPeak = field.getTheNearestPeakDifferentFromThisOne(newTruck.getCurrentPosition(), newTruck.getCurrentPosition());
			if(nextPeak == null)
				break;
			newTruck.move(nextPeak, field.getQuantity(newTruck.getCurrentPosition()));
			field.update(newTruck.getLastMovement());
		}
		field.decrement(newTruck.getCurrentPosition(), newTruck.capacity);
		return newTruck.path;
	}
	
	/**
	 * Gets all the chains of peaks.
	 * 
	 * @param from the coordinates from which search the first chain of peaks.
	 * @return an ArrayList of paths
	 */
	
	private ArrayList <Path> getAllChainsOfPeaks(Coordinates from) {
		ArrayList <Path> chainsOfPeaks = new ArrayList <> ();
		Coordinates nextPeak = field.getTheNearestHole(from);
		while(nextPeak != null) {
			Path chain = getChainOfPeaks(nextPeak, truck.capacity);
			chainsOfPeaks.add(chain);
			nextPeak = field.getTheNearestPeak(chain.getLastCoordinates());
		}
		return chainsOfPeaks;
	}
	
	/**
	 * Creates a path of holes to spread the required quantity of terrain.
	 * 
	 * @param from the coordinates from which search the first hole.
	 * @param quantity the quantity of terrain we want to spread along the chain.
	 * @return the path.
	 */
	
	private Path getChainOfHoles(Coordinates from, long quantity) {
		/*
		 * PAY ATTENTION: the truck has to bring in the first cell "quantity" units of terrain 
		 * to fill the chain of holes.
		 */
		
		Truck newTruck = new Truck(quantity, field.getTheNearestHole(from), quantity);
		field.increment(newTruck.getCurrentPosition(), newTruck.capacity);
		while(field.getQuantity(newTruck.getCurrentPosition()) > 0) {
			Coordinates nextHole = field.getTheNearestHole(newTruck.getCurrentPosition());
			if(nextHole == null)
				break;
			newTruck.move(nextHole, field.getQuantity(newTruck.getCurrentPosition()));
			field.update(newTruck.getLastMovement());
		}
		return newTruck.path;
	}
	
	/**
	 * Gets all the chains of peaks.
	 * 
	 * @param from the coordinates from which search the first chain of peaks.
	 * @return an ArrayList of paths.
	 */
	
	private ArrayList <Path> getAllChainsOfHoles(Coordinates from) {
		ArrayList <Path> chainsOfHoles = new ArrayList <> ();
		Coordinates nextHole = field.getTheNearestHole(from);
		while(nextHole != null) {
			Path chain = getChainOfHoles(nextHole, truck.capacity);
			chainsOfHoles.add(chain);
			nextHole = field.getTheNearestHole(chain.getLastCoordinates());
		}
		return chainsOfHoles;
	}
	
	/**
	 * Moves the minimum quantity of terrain necessary to have 
	 * "field.terrainToMove()" multiple of "truck.capacity".
	 */
	
	private void fixField() {
		long remainder = field.terrainToMove() % truck.capacity;
		if(remainder == 0)
			return;
		Path chainOfPeaks = getChainOfPeaks(truck.getCurrentPosition(), remainder);
		Path chainOfHoles = getChainOfHoles(chainOfPeaks.getLastCoordinates(), remainder);
		truck.move(chainOfPeaks);
		truck.move(chainOfHoles);
	}
	
	/**
	 * For each chain of peaks p[i] and for each chain of holes h[j], matrix[i][j] will contain 
	 * the distance from the last coordinates of p[i] to the first coordinates of h[j].
	 * 
	 * @param chainsOfPeaks
	 * @param chainsOfHoles
	 * @return the matrix of distances.
	 */
	
	private static double[][] buildMatrixOfDistances(ArrayList <Path> chainsOfPeaks, ArrayList <Path> chainsOfHoles) {
		double[][] matrix = new double[chainsOfPeaks.size()][chainsOfHoles.size()];
		for(int i = 0; i < chainsOfPeaks.size(); i ++) 
			for(int j = 0; j < chainsOfHoles.size(); j ++)
				matrix[i][j] = chainsOfPeaks.get(i).getLastCoordinates().distance(chainsOfHoles.get(j).getFirstCoordinates());
		return matrix;
	}
	
	/**
	 * For each chain c[i] and for each chain c[j] matrix[i][j] will contain:
	 * - the distance from the last coordinates of c[i] to the first coordinates of c[j] if min(i, j) < threshold && max(i, j) >= threshold.
	 * - INF otherwise
	 * 
	 * @param chains the nodes of the bipartite graph.
	 * @param threshold is the position of the first chain which belongs to the second set.
	 * @return the matrix of distances.
	 */
	
	private static double[][] buildMatrixOfDistances(ArrayList <Path> chains, int threshold) {
		/*
		 * Bipartite graph
		 */
		
		final double INF = 1E6;
		double[][] matrix = new double[chains.size()][chains.size()];
		for(int i = 0; i < threshold; i ++) 
			for(int j = 0; j < chains.size(); j ++)
				matrix[i][j] = j >= threshold ? chains.get(i).getLastCoordinates().distance(chains.get(j).getFirstCoordinates()) : INF;
		for(int i = threshold; i < chains.size(); i ++) 
			for(int j = 0; j < chains.size(); j ++)
				matrix[i][j] = j < threshold ? chains.get(i).getLastCoordinates().distance(chains.get(j).getFirstCoordinates()) : INF;
		return matrix;
	}
	
	/**
	 * Returns the angle executed by the truck (arrived to c2 from c1) to go to c3.
	 * 
	 * @param c1
	 * @param c2
	 * @param c3
	 * @return the angle.
	 */
	
	public static double getAngle(Coordinates c1, Coordinates c2, Coordinates c3) {
		return c2.subtract(c1).getAngle(c3.subtract(c2));
	}
	
	/**
	 * Checks the angle. It should be between 0 and PI/2 (inclusive).
	 * The "ACCEPTED_ERROR" is for the floating point approximation.
	 * 
	 * @param angle
	 * @return True if the angle is acceptable. False otherwise.
	 */
	
	public static boolean isOk(double angle) {
		return angle <= Math.PI / 2 + ACCEPTED_ERROR;
	}
	
	/*
	 * With this LENGTH we do not need to check if the chosen stopover 
	 * is contained into the field. Indeed we are assuming the field is 
	 * a convex polygon. This means that for each c1, c2 into the field the segment 
	 * which connects them has to be part of the field.
	 * 
	 * Let's consider the worst case can arise: getAngle(c1, c2, c3) = 135�
	 * This means a primary turn of 45� followed by one of 90�. c2.distance(s2) = sqrt(2)LENGTH.
	 * 
	 * However...
	 * 
	 * c3  ?
	 * c1 c2
	 * 
	 * The UP-RIGHT corner of c2 and c3 have to be connected by a segment which traverses the 
	 * center of '?' cell (which contains s2 of course). 
	 * Now because sqrt(2)LENGTH <= 2LENGTH <= c2.distance(?) we know that s2 has to be 
	 * part of the field.
	 */
	
	/**
	 * Returns the stopover to add after c2 to fix the angle if a single stopover is enough 
	 * to solve it. Returns null otherwise.
	 * 
	 * @param c1
	 * @param c2
	 * @param c3
	 * @return the stopover (coordinates) to add after c2 or null.
	 */
	
	private Coordinates singleStopover(Coordinates c1, Coordinates c2, Coordinates c3) {
		final double LENGTH = Math.min(field.deltaX, field.deltaY) / 2;
		double angle = getAngle(c1, c2, c3);
		if(angle > 0.75 * Math.PI)
			return null;
		Vector2D v = c3.subtract(c2);
		for(Vector2D dir : v.getVectorsByAngle(angle - Math.PI / 2, LENGTH)) {
			Coordinates s = new Coordinates(c2, dir);
			
			if(!isOk(getAngle(c1, c2, s))) // turn on the other side
				continue;
			if(!isOk(getAngle(c2, s, c3))) 
				continue;
			
			return s;
		}
		return null;
	}
	
	/**
	 * Returns two stopovers to add after c2 (in the given order) to fix the angle.
	 * 
	 * @param c1
	 * @param c2
	 * @param c3
	 * @return two stopovers (coordinates) to add (in order) after c2.
	 */
	
	private ArrayList <Coordinates> twoStopovers(Coordinates c1, Coordinates c2, Coordinates c3) {
		final double LENGTH = Math.min(field.deltaX, field.deltaY) / 2;
		double angle = getAngle(c1, c2, c3);
		Vector2D v = c3.subtract(c2);
		for(Vector2D dir1 : v.getVectorsByAngle(angle - Math.PI / 2, LENGTH)) {
			Coordinates s1 = new Coordinates(c2, dir1);
			
			if(!isOk(getAngle(c1, c2, s1))) // turn on the other side
				continue;
			
			Coordinates s2 = new Coordinates(s1, v.setLengthTo(LENGTH));
			
			if(!isOk(getAngle(s1, s2, c3))) 
				continue;
			
			ArrayList <Coordinates> stopovers = new ArrayList <> ();
			stopovers.add(s1);
			stopovers.add(s2);
			return stopovers;
		}
		return null;
	}
	
	/**
	 * For each change it checks if it is acceptable and in case it fixes it.
	 */
	
	private void fixPath() {
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
			truck.path.rerouteTwo(i - 1, stopovers.get(0), stopovers.get(1));
		}
	}
	
	/**
	 * Finds the nearest chain to from (according to its first coordinates). 
	 * The chains marked as "done" are discarded.
	 * 
	 * @param from
	 * @param chains
	 * @param done
	 * @return the index of the nearest.
	 */
	
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
	
	/**
	 * Returns a lower bound for the length of an Hamiltonian cycle of minimum length 
	 * by using an optimal solution for the assignment problem.
	 * 
	 * @param chainsOfPeaks
	 * @param chainsOfHoles
	 * @param assignmentPH an optimal assignment from chains of peaks to chains of holes.
	 * @param assignmentHP an optimal assignment in the other direction.
	 * @return the lower bound.
	 */
	
	private static double getLowerBoundHC(ArrayList <Path> chainsOfPeaks, ArrayList <Path> chainsOfHoles, int[] assignmentPH, int[] assignmentHP) {
		boolean[] doneP = new boolean[chainsOfPeaks.size()];
		double lb = 0.0;
		for(int i = 0; i < chainsOfPeaks.size(); i ++) {
			int j = i;
			while(!doneP[j]) {
				doneP[j] = true;
				lb += chainsOfPeaks.get(j).getLastCoordinates().distance(chainsOfHoles.get(assignmentPH[j]).getFirstCoordinates());
				lb += chainsOfHoles.get(assignmentPH[j]).getLastCoordinates().distance(chainsOfPeaks.get(assignmentHP[assignmentPH[j]]).getFirstCoordinates());
				j = assignmentHP[assignmentPH[j]];
			}
		}
		return lb;
	}
	
	/**
	 * Returns the cost of the Hamiltonian cycle we have obtained (by using our heuristic).
	 * 
	 * @param chainsOfPeaks
	 * @param chainsOfHoles
	 * @param first
	 * @param p
	 * @return the cost of the Hamiltonian cycle we have obtained.
	 */
	
	private static double getCurrentHC(ArrayList <Path> chainsOfPeaks, ArrayList <Path> chainsOfHoles, int first, Path p) {
		double current = p.distance();
		
		current -= p.prefix(first + 1).distance();
		for(Path chain : chainsOfPeaks)
			current -= chain.distance();
		for(Path chain : chainsOfHoles)
			current -= chain.distance();
		
		current += p.getLastCoordinates().distance(p.getCoordinates(first));
		
		return current;
	}
	
	/**
	 * Swaps elements of the permutation until it reaches a local minimum.
	 * 
	 * @param permutation
	 * @param chainsOfPeaks
	 * @param chainsOfHoles
	 */
	
	private static void improvePath(ArrayList <Integer> permutation, ArrayList <Path> chainsOfPeaks, ArrayList <Path> chainsOfHoles) {
		
	}
	
	/**
	 * Solves the problem by using LKH algorithm.
	 * 
	 * @return the path.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	
	public Path solveWithLKH() throws IOException, InterruptedException {
		fixField();
		ArrayList <Path> chainsOfPeaks = getAllChainsOfPeaks(truck.getCurrentPosition());
		ArrayList <Path> chainsOfHoles = getAllChainsOfHoles(truck.getCurrentPosition());
		assert(chainsOfPeaks.size() == chainsOfHoles.size());
		if(chainsOfPeaks.size() > 0) {
			ArrayList <Path> chains = new ArrayList <> ();
			chains.addAll(chainsOfPeaks);
			chains.addAll(chainsOfHoles);
			for(int pi : LKH_Manager.getPermutation(buildMatrixOfDistances(chains, chainsOfPeaks.size())))
				truck.move(chains.get(pi));
		}
		fixPath();
		return truck.path;
	}
	
	/**
	 * Solves the problem by using our heuristic.
	 * 
	 * @return the path.
	 */
	
	public Path solveWithImprovedNearestNeighbourStrategy() {
		fixField();
		ArrayList <Path> chainsOfPeaks = getAllChainsOfPeaks(truck.getCurrentPosition());
		ArrayList <Path> chainsOfHoles = getAllChainsOfHoles(truck.getCurrentPosition());
		assert(chainsOfPeaks.size() == chainsOfHoles.size());
		if(chainsOfPeaks.size() > 0) {
			int[] assignmentPH = new HungarianAlgorithm(buildMatrixOfDistances(chainsOfPeaks, chainsOfHoles)).execute();
			int[] assignmentHP = new HungarianAlgorithm(buildMatrixOfDistances(chainsOfHoles, chainsOfPeaks)).execute();
			boolean[] doneP = new boolean[chainsOfPeaks.size()];
			
			ArrayList <Integer> permutation = new ArrayList <> ();
			
			int first = truck.path.length();
			int next = getTheIndexOfTheNearest(truck.getCurrentPosition(), chainsOfPeaks, doneP);
			while(next != -1) {
				doneP[next] = true;
				permutation.add(next);
				permutation.add(assignmentPH[next]);
				next = doneP[assignmentHP[assignmentPH[next]]] ? getTheIndexOfTheNearest(chainsOfHoles.get(assignmentPH[next]).getLastCoordinates(), chainsOfPeaks, doneP) :
						assignmentHP[assignmentPH[next]];
			}
			
			improvePath(permutation, chainsOfPeaks, chainsOfHoles);
			
			double lowerBoundHC = getLowerBoundHC(chainsOfPeaks, chainsOfHoles, assignmentPH, assignmentHP);
			double currentHC = getCurrentHC(chainsOfPeaks, chainsOfHoles, first, truck.path);
			
			System.out.println("LOWER_BOUND_HC: " + lowerBoundHC + " CURRENT_HC: " + currentHC + " ERROR: " + (((currentHC - lowerBoundHC) / lowerBoundHC) * 100) + "%");
		}
		fixPath();
		return truck.path;
	}
}

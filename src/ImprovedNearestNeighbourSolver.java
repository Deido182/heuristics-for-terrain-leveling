import java.util.ArrayList;

public class ImprovedNearestNeighbourSolver implements Solver {
	
	Field field;
	Truck truck;
	
	public ImprovedNearestNeighbourSolver(Field field, Truck truck) {
		this.field = field;
		this.truck = truck;
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
	 * Solves the problem by using our heuristic.
	 * 
	 * @return the path.
	 */
	
	public Path solve() {
		ChainsBuilder chainsBuilder = new NearestNeighbourChainsBuilder(field, truck);
		chainsBuilder.fixField();
		ArrayList <Path> chainsOfPeaks = chainsBuilder.getAllChainsOfPeaks(truck.getCurrentPosition());
		ArrayList <Path> chainsOfHoles = chainsBuilder.getAllChainsOfHoles(truck.getCurrentPosition());
		assert(chainsOfPeaks.size() == chainsOfHoles.size());
		if(chainsOfPeaks.size() > 0) {
			int[] assignmentPH = new HungarianAlgorithm(buildMatrixOfDistances(chainsOfPeaks, chainsOfHoles)).execute();
			int[] assignmentHP = new HungarianAlgorithm(buildMatrixOfDistances(chainsOfHoles, chainsOfPeaks)).execute();
			boolean[] doneP = new boolean[chainsOfPeaks.size()];
			
			ArrayList <Path> chains = new ArrayList <> ();
			
			int first = truck.path.length();
			int next = getTheIndexOfTheNearest(truck.getCurrentPosition(), chainsOfPeaks, doneP);
			while(next != -1) {
				doneP[next] = true;
				chains.add(chainsOfPeaks.get(next));
				chains.add(chainsOfHoles.get(assignmentPH[next]));
				next = doneP[assignmentHP[assignmentPH[next]]] ? getTheIndexOfTheNearest(chainsOfHoles.get(assignmentPH[next]).getLastCoordinates(), chainsOfPeaks, doneP) :
						assignmentHP[assignmentPH[next]];
			}
			
			truck.improveSequenceOfChains(chains);
			for(Path chain : chains)
				truck.move(chain);
			
			double lowerBoundHC = getLowerBoundHC(chainsOfPeaks, chainsOfHoles, assignmentPH, assignmentHP);
			double currentHC = getCurrentHC(chainsOfPeaks, chainsOfHoles, first, truck.path);
			
			System.out.println("LOWER_BOUND_HC: " + lowerBoundHC + " CURRENT_HC: " + currentHC + " ERROR: " + (((currentHC - lowerBoundHC) / lowerBoundHC) * 100) + "%");
		}
		truck.fixPath(Math.min(field.deltaX, field.deltaY) / 2);
		return truck.path;
	}
	
	/*
	 * With this LENGTH we do not need to check if the chosen stopover 
	 * is contained into the field. Indeed we are assuming the field is 
	 * a convex polygon. This means that for each c1, c2 into the field the segment 
	 * which connects them has to be part of the field.
	 * 
	 * Let's consider the worst case can arise: getAngle(c1, c2, c3) = 135°
	 * This means a primary turn of 45° followed by one of 90°. c2.distance(s2) = sqrt(2)LENGTH.
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
}

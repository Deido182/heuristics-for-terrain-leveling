import java.util.ArrayList;

public class OurSolver implements Solver {
	
	Field field;
	Truck truck;
	ChainsBuilder chainsBuilder;
	
	public OurSolver(Field field, Truck truck, ChainsBuildersFactory factory) {
		this.field = field;
		this.truck = truck;
		this.chainsBuilder = factory.getChainsBuilder(field, truck);
	}
	
	/**
	 * For each chain of peaks p[i] and for each chain of holes h[j], matrix[i][j] will contain 
	 * the distance from the last coordinates of p[i] to the first coordinates of h[j].
	 * 
	 * @param chainsOfPeaks
	 * @param chainsOfHoles
	 * @return the matrix of distances.
	 */
	
	private double[][] buildMatrixOfDistances(ArrayList <Path> chainsOfPeaks, ArrayList <Path> chainsOfHoles) {
		double[][] matrix = new double[chainsOfPeaks.size()][chainsOfHoles.size()];
		for(int i = 0; i < chainsOfPeaks.size(); i ++) 
			for(int j = 0; j < chainsOfHoles.size(); j ++)
				matrix[i][j] = chainsOfPeaks.get(i).distance(truck, chainsOfHoles.get(j));
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
	
	private int getTheIndexOfTheNearest(Path from, ArrayList <Path> chains, boolean[] done) {
		int nearest = -1;
		for(int i = 0; i < chains.size(); i ++)
			if(!done[i]) {
				if(nearest == -1)
					nearest = i;
				else if(from.distance(truck, chains.get(i)) < from.distance(truck, chains.get(nearest)))
					nearest = i;
			}
		return nearest;
	}
	
	/**
	 * Solves the problem by using our heuristic.
	 * 
	 * @return the path.
	 */
	
	public Path solve() {
		chainsBuilder.fixField();
		ArrayList <Path> chainsOfPeaks = chainsBuilder.getAllChainsOfPeaks(truck.path);
		ArrayList <Path> chainsOfHoles = chainsBuilder.getAllChainsOfHoles(truck.path);
		assert(chainsOfPeaks.size() == chainsOfHoles.size());
		if(chainsOfPeaks.size() > 0) {
			int[] assignmentPH = new HungarianAlgorithm(buildMatrixOfDistances(chainsOfPeaks, chainsOfHoles)).execute();
			int[] assignmentHP = new HungarianAlgorithm(buildMatrixOfDistances(chainsOfHoles, chainsOfPeaks)).execute();
			boolean[] doneP = new boolean[chainsOfPeaks.size()];
			
			ArrayList <Path> chains = new ArrayList <> ();
			
			int next = getTheIndexOfTheNearest(truck.path, chainsOfPeaks, doneP);
			while(next != -1) {
				doneP[next] = true;
				chains.add(chainsOfPeaks.get(next));
				chains.add(chainsOfHoles.get(assignmentPH[next]));
				next = doneP[assignmentHP[assignmentPH[next]]] ? getTheIndexOfTheNearest(chainsOfHoles.get(assignmentPH[next]), chainsOfPeaks, doneP) :
						assignmentHP[assignmentPH[next]];
			}

			truck.improveSequenceOfChains(chains);
			for(Path chain : chains)
				truck.move(chain);
		}
		truck.fixPath();
		return truck.path;
	}
}

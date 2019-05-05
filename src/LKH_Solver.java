import java.io.IOException;
import java.util.ArrayList;

public class LKH_Solver implements Solver {

	Field field;
	Truck truck;
	ChainsBuilder chainsBuilder;
	
	public LKH_Solver(Field field, Truck truck, ChainsBuildersFactory factory) {
		this.field = field;
		this.truck = truck;
		this.chainsBuilder = factory.getChainsBuilder(field, truck);
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
	 * Solves the problem by using LKH algorithm.
	 * 
	 * @return the path.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	
	public Path solve() {
		try {
			chainsBuilder.fixField();
			ArrayList <Path> chainsOfPeaks = chainsBuilder.getAllChainsOfPeaks(truck.path);
			ArrayList <Path> chainsOfHoles = chainsBuilder.getAllChainsOfHoles(truck.path);
			assert(chainsOfPeaks.size() == chainsOfHoles.size());
			if(chainsOfPeaks.size() > 0) {
				ArrayList <Path> chains = new ArrayList <> ();
				chains.addAll(chainsOfPeaks);
				chains.addAll(chainsOfHoles);
				for(int pi : LKH_Manager.getPermutation(buildMatrixOfDistances(chains, chainsOfPeaks.size())))
					truck.move(chains.get(pi));
			}
			truck.fixPath();
			return truck.path;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

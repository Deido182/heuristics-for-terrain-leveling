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
	
	public Path solve() throws IOException, InterruptedException {
		chainsBuilder.fixField();
		ArrayList <Path> chainsOfPeaks = chainsBuilder.getAllChainsOfPeaks(truck.getCurrentPosition());
		ArrayList <Path> chainsOfHoles = chainsBuilder.getAllChainsOfHoles(truck.getCurrentPosition());
		assert(chainsOfPeaks.size() == chainsOfHoles.size());
		if(chainsOfPeaks.size() > 0) {
			ArrayList <Path> chains = new ArrayList <> ();
			chains.addAll(chainsOfPeaks);
			chains.addAll(chainsOfHoles);
			for(int pi : LKH_Manager.getPermutation(buildMatrixOfDistances(chains, chainsOfPeaks.size())))
				truck.move(chains.get(pi));
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

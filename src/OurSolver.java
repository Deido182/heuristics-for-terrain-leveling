import java.awt.Color;
import java.io.IOException;
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
	
	private double[][] buildMatrixOfDistances(ArrayList <Truck> chainsOfPeaks, ArrayList <Truck> chainsOfHoles) {
		double[][] matrix = new double[chainsOfPeaks.size()][chainsOfHoles.size()];
		for(int i = 0; i < chainsOfPeaks.size(); i ++) 
			for(int j = 0; j < chainsOfHoles.size(); j ++)
				matrix[i][j] = chainsOfPeaks.get(i).distance(chainsOfHoles.get(j).path);
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
	
	private int getTheIndexOfTheNearest(Truck lastTruck, ArrayList <Truck> chains, boolean[] done) {
		int nearest = -1;
		for(int i = 0; i < chains.size(); i ++)
			if(!done[i]) {
				if(nearest == -1)
					nearest = i;
				else if(lastTruck.distance(chains.get(i).path) < lastTruck.distance(chains.get(nearest).path))
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
		new PathPrinter("").print(field, truck.path, 2.1, 2.1, 200.0, 200.0, Color.black);
		ArrayList <Truck> chainsOfPeaks = chainsBuilder.getAllChainsOfPeaks();
		System.out.println(chainsOfPeaks.size());
		try {
			new PathPrinter("").printChains(field, chainsOfPeaks, 2.1, 2.1, 200.0, 200.0, "chainsOfPeaks.png", Color.BLACK, Color.GREEN);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList <Truck> chainsOfHoles = chainsBuilder.getAllChainsOfHoles();
		try {
			new PathPrinter("").printChains(field, chainsOfHoles, 2.1, 2.1, 200.0, 200.0, "chainsOfHoles.png", Color.BLACK, Color.GREEN);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assert(chainsOfPeaks.size() == chainsOfHoles.size());
		if(chainsOfPeaks.size() > 0) {
			int[] assignmentPH = new HungarianAlgorithm(buildMatrixOfDistances(chainsOfPeaks, chainsOfHoles)).execute();
			int[] assignmentHP = new HungarianAlgorithm(buildMatrixOfDistances(chainsOfHoles, chainsOfPeaks)).execute();
			boolean[] doneP = new boolean[chainsOfPeaks.size()];
			
			ArrayList <Truck> chains = new ArrayList <> ();
			
			int next = getTheIndexOfTheNearest(truck, chainsOfPeaks, doneP);
			while(next != -1) {
				doneP[next] = true;
				chains.add(chainsOfPeaks.get(next));
				chains.add(chainsOfHoles.get(assignmentPH[next]));
				next = doneP[assignmentHP[assignmentPH[next]]] ? getTheIndexOfTheNearest(chainsOfHoles.get(assignmentPH[next]), chainsOfPeaks, doneP) :
						assignmentHP[assignmentPH[next]];
			}

			truck.improveSequenceOfChains(chains);
			for(Truck chain : chains)
				truck.move(chain.path);
		}
		truck.fixPath();
		return truck.path;
	}
}

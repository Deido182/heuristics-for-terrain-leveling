
public class GRASP_Solver implements Solver {
	
	Field field;
	Truck truck;
	final int TIMES1 = 5;
	final int TIMES2 = 50;
	final double THRESHOLD = 1E-5;
	
	public GRASP_Solver(Field field, Truck truck) {
		this.field = field;
		this.truck = truck;
	}
	
	public Path solve() {
		double alpha = 0.1;
		OurSolver ourSolver = new OurSolver(field.clone(), truck.clone(), new GRASP_Factory());
		((GRASP_ChainsBuilder)(ourSolver.chainsBuilder)).alpha = alpha;
		double bestAlpha = alpha;
		Path bestPath = ourSolver.solve();
		
		while(alpha > THRESHOLD) {
			System.out.println(alpha + " " + bestPath.length());
			for(int i = 0; i < TIMES1; i ++) {
				ourSolver = new OurSolver(field.clone(), truck.clone(), new GRASP_Factory());
				((GRASP_ChainsBuilder)(ourSolver.chainsBuilder)).alpha = alpha;
				Path currPath = ourSolver.solve();
				
				if(currPath.length() < bestPath.length()) {
					bestPath = currPath;
					bestAlpha = alpha;
				}
			}
			alpha /= 2.0;
		}
		
		for(int i = 0; i < TIMES2; i ++) {
			ourSolver = new OurSolver(field.clone(), truck.clone(), new GRASP_Factory());
			((GRASP_ChainsBuilder)(ourSolver.chainsBuilder)).alpha = bestAlpha;
			Path currPath = ourSolver.solve();
			
			if(currPath.length() < bestPath.length()) 
				bestPath = currPath;
		}
		
		Path NN_Path = new OurSolver(field.clone(), truck.clone(), new NearestNeighbourFactory()).solve();
		
		if(NN_Path.length() < bestPath.length()) 
			bestPath = NN_Path;
		
		return bestPath;
	}
}

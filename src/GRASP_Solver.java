import java.io.IOException;

public class GRASP_Solver implements Solver {
	
	Field field;
	Truck truck;
	final int TIMES1 = 20;
	final int TIMES2 = 100;
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
			System.out.println(alpha + " " + bestPath.distance());
			for(int i = 0; i < TIMES1; i ++) {
				ourSolver = new OurSolver(field.clone(), truck.clone(), new GRASP_Factory());
				((GRASP_ChainsBuilder)(ourSolver.chainsBuilder)).alpha = alpha;
				Path currPath = ourSolver.solve();
				
				if(currPath.distance() < bestPath.distance()) {
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
			
			if(currPath.distance() < bestPath.distance()) 
				bestPath = currPath;
		}
		
		Path NN_Path = new OurSolver(field.clone(), truck.clone(), new NearestNeighbourFactory()).solve();
		
		if(NN_Path.distance() < bestPath.distance()) 
			bestPath = NN_Path;
		
		return bestPath;
	}
}

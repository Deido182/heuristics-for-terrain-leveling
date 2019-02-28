import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws IOException {
		double[] capacities = new double[] {0.015, 0.025, 0.040};
		final double MINIMUM_MOVE = 1.5;
		
		for(int inputCode = 1; inputCode <= 3; inputCode ++) {
			for(double capacity : capacities) {
				Field field = new Field(new Scanner(new FileReader(new File("cellplot" + inputCode + "b.txt"))));
				Field clone = new Field(new Scanner(new FileReader(new File("cellplot" + inputCode + "b.txt")))); // just to be sure
				Truck truck = new Truck(capacity, MINIMUM_MOVE, null);
				
				long start = System.currentTimeMillis();
				
				ArrayList <Movement> path = new Solver(field, truck).solve();
				
				new PathPrinter().print(path, 5.0, 2.0, "PathPrinted\\PATH_cellplot" + inputCode + "b_" + capacity + "_.png");
				
				long stop = System.currentTimeMillis();
				
				assert(field.isSmooth());
				assert(!clone.isSmooth());
				
				double distance = 0.0;
				for(Movement m : path) {
					assert(field.contains(m.from) && field.contains(m.to));
					assert(0.0 <= m.quantity && m.quantity <= capacity);
					assert(m.distance() >= truck.minimumMove - Solver.ACCEPTED_ERROR);
					
					clone.update(m);
					distance += m.distance();
				}

				assert(clone.isSmooth());
				
				for(int i = 1; i < path.size(); i ++)
					assert(path.get(i - 1).to.equals(path.get(i).from));

				for(int i = 1; i < path.size(); i ++)
					assert(Solver.isOk(Solver.getAngle(path.get(i - 1), path.get(i))));
				
				System.out.println("cellplot" + inputCode + "b / capacity = " + capacity + ":\nMovements: " + path.size() + "\nDistance: " + distance + "m");
				System.out.println("Time: " + (stop - start) + "ms\n");
			}
			System.out.print("#############################\n\n");
		}
	}
}

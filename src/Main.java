import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws IOException {
		final double[] capacities = new double[] {0.015, 0.025, 0.040, 0.080};
		final Coordinates TRUCK_STARTING_POINT = new Coordinates(0.0, 0.0);
		final double INITIAL_CARGO = 0.0;
		
		for(int inputCode = 1; inputCode <= 4; inputCode ++) {
			for(double capacity : capacities) {
				Field field = new Field(new Scanner(new FileReader(new File("cellplot" + inputCode + "b.txt"))));
				Field clone = new Field(new Scanner(new FileReader(new File("cellplot" + inputCode + "b.txt")))); // just to be sure
				Truck truck = new Truck(capacity, TRUCK_STARTING_POINT, INITIAL_CARGO);
				
				long start = System.currentTimeMillis();
				
				Path path = new Solver(field, truck).solve();
				
				//new PathPrinter().print(field, path, 2.1, 2.1, 25.0, 25.0, "PathPrinted\\PATH_cellplot" + inputCode + "b_" + capacity + "_.png");
				//new PathPrinter().print(field, path, 2.1, 2.1, 25.0, 25.0);
				
				long stop = System.currentTimeMillis();
				
				assert(field.isSmooth());
				assert(!clone.isSmooth());
				
				for(Stopover s : path.stopovers) 
					assert(0.0 <= s.quantityToBringIn && s.quantityToBringIn <= capacity);
				
				for(int i = 0; i < path.length() - 1; i ++)
					clone.update(truck.getMovement(i));
				
				assert(clone.isSmooth());
				
				for(int i = 0; i < path.length() - 2; i ++)
					assert(truck.getMovement(i).to.equals(truck.getMovement(i + 1).from));

				for(int i = 2; i < truck.path.length(); i ++)
					assert(Solver.isOk(Solver.getAngle(truck.path.getCoordinates(i - 2), truck.path.getCoordinates(i - 1), truck.path.getCoordinates(i))));
				
				System.out.println("cellplot" + inputCode + "b / capacity = " + capacity + ":\nMovements: " + path.length() + "\nDistance: " + path.distance() + "m");
				System.out.println("Time: " + (stop - start) + "ms\n");
			}
			System.out.print("#############################\n\n");
		}
	}
}

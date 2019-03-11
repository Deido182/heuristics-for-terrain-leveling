import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Main {
	
	public static void print(Path p, String filePath) throws IOException {
		PrintWriter out = new PrintWriter(new FileWriter(new File(filePath)));
		for(Stopover s : p.stopovers)
			out.println(s.coordinates.x + " " + s.coordinates.y + " " + s.quantityToBringIn);
		out.close();
	}
	
	public static void oneMoreCheck(String input, String output) throws FileNotFoundException {
		Field field = new Field(new Scanner(new FileReader(new File(input))));
		Scanner scanner = new Scanner(new FileReader(new File(output)));
		String[] tok = scanner.nextLine().split(" ");
		Coordinates last = new Coordinates(Double.parseDouble(tok[0]), Double.parseDouble(tok[1]));
		Double.parseDouble(tok[2]); // discard the first quantity
		while(scanner.hasNextLine()) {
			tok = scanner.nextLine().split(" ");
			Coordinates curr = new Coordinates(Double.parseDouble(tok[0]), Double.parseDouble(tok[1]));
			double quantityToBringIn = Double.parseDouble(tok[2]);
			field.decrement(last, quantityToBringIn);
			field.increment(curr, quantityToBringIn);
			last = curr;
		}
		assert(field.isSmooth());
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		final double[] capacities = new double[] {0.015, 0.025, 0.040};
		final Coordinates TRUCK_STARTING_POINT = new Coordinates(0.0, 0.0);
		final double INITIAL_CARGO = 0.0;
		
		/*
		 * OFFICIAL
		 */
		
		for(int inputCode = 1; inputCode <= 4; inputCode ++) {
			for(int j = 0; j < capacities.length; j ++) {
				double capacity = capacities[j];
				
				final String INPUT = "Input\\cellplot" + inputCode + "b.txt";
				final String OUTPUT = "Output\\cellplot" + inputCode + "b.path";
				
				Field field = new Field(new Scanner(new FileReader(new File(INPUT))));
				Field clone = new Field(new Scanner(new FileReader(new File(INPUT)))); // just to be sure
				Truck truck = new Truck(capacity, TRUCK_STARTING_POINT, INITIAL_CARGO);
				
				long start = System.currentTimeMillis();
				
				//Path path = new Solver(field, truck).solveWithLKH();
				Path path = new Solver(field, truck).solveWithNearestNeighbourStrategy();
				
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
				
				print(path, OUTPUT);
				oneMoreCheck(INPUT, OUTPUT);
			}
			System.out.print("#############################\n\n");
		}
		
		/*
		 * NEW
		 */
		
		//new InputBuilder("Input\\in", 30, 30, new Coordinates(10.0, 10.0), 10.0, 7.0, 0.200).build(5);
		
		for(int inputCode = 1; inputCode <= 5; inputCode ++) {
			for(int j = 2; j < capacities.length; j ++) {
				double capacity = capacities[j];
				
				final String INPUT = "Input\\in" + inputCode;
				final String OUTPUT = "Output\\in" + inputCode + ".path";
				
				Field field = new Field(new Scanner(new FileReader(new File(INPUT))));
				Field clone = new Field(new Scanner(new FileReader(new File(INPUT)))); // just to be sure
				Truck truck = new Truck(capacity, TRUCK_STARTING_POINT, INITIAL_CARGO);
				
				long start = System.currentTimeMillis();
				
				//Path path = new Solver(field, truck).solveWithLKH();
				Path path = new Solver(field, truck).solveWithNearestNeighbourStrategy();
				
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
				
				System.out.println("in" + inputCode + " / capacity = " + capacity + ":\nMovements: " + path.length() + "\nDistance: " + path.distance() + "m");
				System.out.println("Time: " + (stop - start) + "ms\n");
				
				print(path, OUTPUT);
				oneMoreCheck(INPUT, OUTPUT);
			}
			System.out.print("#############################\n\n");
		}
	}
}

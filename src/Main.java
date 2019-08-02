import java.awt.Color;
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
			long quantityToBringIn = Long.parseLong(tok[2]);
			field.decrement(last, quantityToBringIn);
			field.increment(curr, quantityToBringIn);
			last = curr;
		}
		assert(field.isSmooth());
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		final long[] capacities = new long[] {150, 250, 400}; // REMEMBER Field.PRECISION = 10000 (NOT 1000)
		final Coordinates TRUCK_STARTING_POINT = new Coordinates(0.0, 0.0);
		final long INITIAL_CARGO = 0;
		
		/*
		 * OFFICIAL
		 */
		
		for(int inputCode = 5; inputCode <= 4; inputCode ++) {
			for(int j = 0; j < capacities.length; j ++) {
				long capacity = capacities[j];
				
				final String INPUT = "Input\\cellplot" + inputCode + "b.txt";
				final String OUTPUT = "Output\\cellplot" + inputCode + "b.path";
				
				Field field = new Field(new Scanner(new FileReader(new File(INPUT))));
				Field clone = new Field(new Scanner(new FileReader(new File(INPUT)))); // just to be sure
				
				final double GAMMA = Math.PI / 4.0;
				final double S = Math.min(field.deltaX, field.deltaY);
				
				Truck truck = new Truck(capacity, GAMMA, S, TRUCK_STARTING_POINT, INITIAL_CARGO);
				
				long start = System.currentTimeMillis();

				//Path path = new LKH_Solver(field, truck, new NearestNeighbourFactory()).solve();
				Path path = new OurSolver(field, truck, new NearestNeighbourFactory()).solve();
				//Path path = new GRASP_Solver(field, truck).solve();
						
				//new PathPrinter("").print(field, path, 2.1, 2.1, 25.0, 25.0, "PathPrinted\\PATH_cellplot" + inputCode + "b_" + capacity + "_.png");
				//new PathPrinter("").print(field, path, 2.1, 2.1, 25.0, 25.0);
				
				long stop = System.currentTimeMillis();
				
				truck.path = path;
				assert(!clone.isSmooth());
				
				for(Stopover s : path.stopovers) 
					assert(0 <= s.quantityToBringIn && s.quantityToBringIn <= capacity);
				
				for(int i = 0; i < path.size() - 1; i ++)
					clone.update(truck.getMovement(i));
				
				assert(clone.isSmooth());
				
				for(int i = 0; i < path.size() - 2; i ++)
					assert(truck.getMovement(i).to.equals(truck.getMovement(i + 1).from));

				for(int i = 2; i < truck.path.size(); i ++)
					assert(truck.angleOk(Truck.getAngle(truck.path.getCoordinates(i - 2), truck.path.getCoordinates(i - 1), truck.path.getCoordinates(i))));
				
				/*
				 * From i = 2 because the first movement is from (0, 0) which is the starting point 
				 * for the truck. It could be a point which does not belong to the field.
				 * It is not important if this movement is < S.
				 */
				
				for(int i = 2; i < truck.path.size(); i ++) 
					assert(truck.movementOk(truck.path.getCoordinates(i - 1), truck.path.getCoordinates(i)));
				
				System.out.println("cellplot" + inputCode + "b / capacity = " + capacity + ":\nMovements: " + path.size() + "\nDistance: " + path.length() + "m");
				System.out.println("Time: " + (stop - start) + "ms\n");
				
				print(path, OUTPUT);
				oneMoreCheck(INPUT, OUTPUT);
			}
			System.out.print("#############################\n\n");
		}
		
		/*
		 * NEW
		 */
		
		//new InputBuilder("Input\\in", 4, 4, new Coordinates(400.0, 400.0), 40.0, 40.0, ((double)capacities[2]) / Field.PRECISION).build(1);
		
		for(int inputCode = 1; inputCode <= 1; inputCode ++) {
			for(int j = 2; j < capacities.length; j ++) {
				long capacity = capacities[j];
				
				final String INPUT = "Input\\in" + inputCode;
				final String OUTPUT = "Output\\in" + inputCode + ".path";
				
				Field field = new Field(new Scanner(new FileReader(new File(INPUT))));
				Field clone = new Field(new Scanner(new FileReader(new File(INPUT)))); // just to be sure

				final double GAMMA = Math.PI / 4.0;
				final double S = Math.min(field.deltaX, field.deltaY);
				
				Truck truck = new Truck(capacity, GAMMA, S, TRUCK_STARTING_POINT, INITIAL_CARGO);
				
				long start = System.currentTimeMillis();
				
				//Path path = new LKH_Solver(field, truck, new NearestNeighbourFactory()).solve();
				Path path = new OurSolver(field, truck, new NearestNeighbourFactory()).solve();
				//Path path = new GRASP_Solver(field, truck).solve();
				
				//new PathPrinter("").print(field, path, 2.1, 2.1, 25.0, 25.0, "PathPrinted\\PATH_cellplot" + inputCode + "b_" + capacity + "_.png");
				new PathPrinter("").print(field, path, 2.1, 2.1, 200.0, 200.0, Color.BLACK);
				
				long stop = System.currentTimeMillis();
				
				truck.path = path;
				assert(!clone.isSmooth());
				
				for(Stopover s : path.stopovers) 
					assert(0 <= s.quantityToBringIn && s.quantityToBringIn <= capacity);
				
				for(int i = 0; i < path.size() - 1; i ++)
					clone.update(truck.getMovement(i));
				
				assert(clone.isSmooth());
				
				for(int i = 0; i < path.size() - 2; i ++)
					assert(truck.getMovement(i).to.equals(truck.getMovement(i + 1).from));

				for(int i = 2; i < truck.path.size(); i ++)
					assert(truck.angleOk(Truck.getAngle(truck.path.getCoordinates(i - 2), truck.path.getCoordinates(i - 1), truck.path.getCoordinates(i))));
				
				/*
				 * From i = 2 because the first movement is from (0, 0) which is the starting point 
				 * for the truck. It could be a point which does not belong to the field.
				 * It is not important if this movement is < S.
				 */
				
				for(int i = 2; i < truck.path.size(); i ++) 
					assert(truck.movementOk(truck.path.getCoordinates(i - 1), truck.path.getCoordinates(i)));
				
				System.out.println("in" + inputCode + " / capacity = " + capacity + ":\nMovements: " + path.size() + "\nDistance: " + path.length() + "m");
				System.out.println("Time: " + (stop - start) + "ms\n");
				
				print(path, OUTPUT);
				oneMoreCheck(INPUT, OUTPUT);
			}
			System.out.print("#############################\n\n");
		}
	}
}

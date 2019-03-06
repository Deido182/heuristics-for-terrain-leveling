import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LKH_Manager {
	private static final String TOUR_FILE = "TOUR_FILE";
	private static final String PROBLEM_FILE = "PROBLEM_FILE";
	private static final String PARAMETER_FILE = "PARAMETER_FILE";
	private static final int PRECISION = (int)1E4;
	
	public static int[][] fix(double[][] distances) {
		int[][] fixedDistances = new int[distances.length][distances.length];
		for(int i = 0; i < distances.length; i ++)
			for(int j = 0; j < distances.length; j ++)
				fixedDistances[i][j] = (int)(distances[i][j] * PRECISION);
		return fixedDistances;
	}
	
	public static void writePARAMETER_FILE() throws IOException {
		PrintWriter out = new PrintWriter(new FileWriter(new File(PARAMETER_FILE)));
		
		out.println("PROBLEM_FILE = " + PROBLEM_FILE);
		out.println("TOUR_FILE = " + TOUR_FILE);
		
		out.close();
	}
	
	public static void writePROBLEM_FILE(double[][] distances) throws IOException {
		PrintWriter out = new PrintWriter(new FileWriter(new File(PROBLEM_FILE)));
		
		out.println("NAME: ATSP_between_chains");
		out.println("TYPE: ATSP");
		out.println("DIMENSION: " + distances.length);
		out.println("EDGE_WEIGHT_TYPE: EXPLICIT");
		out.println("EDGE_WEIGHT_FORMAT: FULL_MATRIX");
		out.println("EDGE_WEIGHT_SECTION");
		
		/*
		 * This LKH implementation wants integral values (if provided explicitly)
		 */
		
		int[][] fixedDistances = fix(distances);
		for(int[] a : fixedDistances)
			for(int integralDistance : a)
				out.println(integralDistance);
		
		out.close();
	}
	
	public static void execute() throws IOException {
		Runtime.getRuntime().exec("LKH.exe " + PARAMETER_FILE);
	}
	
	public static void run(double[][] distances) throws IOException {
		writePARAMETER_FILE();
		writePROBLEM_FILE(distances);
		execute();
	}
}

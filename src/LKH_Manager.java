import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LKH_Manager {
	private static final String SEPARATOR = " ";
	private static final int PRECISION = (int)1E4;
	
	public static int[][] fix(double[][] distances) {
		int[][] fixedDistances = new int[distances.length][distances.length];
		for(int i = 0; i < distances.length; i ++)
			for(int j = 0; j < distances.length; j ++)
				fixedDistances[i][j] = (int)(distances[i][j] * PRECISION);
		return fixedDistances;
	}
	
	public static void solve(double[][] distances) throws IOException {
		PrintWriter out = new PrintWriter(new FileWriter(new File("PROBLEM_FILE")));
		
		out.write("NAME: ATSP_between_chains" + SEPARATOR
				+ "TYPE: ATSP" + SEPARATOR
				+ "DIMENSION: " + distances.length + SEPARATOR
				+ "EDGE_WEIGHT_TYPE: EXPLICIT" + SEPARATOR
				+ "EDGE_WEIGHT_FORMAT: FULL_MATRIX" + SEPARATOR
				+ "EDGE_WEIGHT_SECTION" + SEPARATOR);
		
		/*
		 * This LKH implementation wants integral values (if provided explicitly)
		 */
		
		int[][] fixedDistances = fix(distances);
		for(int[] a : fixedDistances)
			for(int distance : a)
				out.write(distance + SEPARATOR);
		
		out.close();
	}
}

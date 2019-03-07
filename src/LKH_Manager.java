import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

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
		Process p = Runtime.getRuntime().exec("LKH.exe " + PARAMETER_FILE);
		while(!new File(TOUR_FILE).exists());
		
		/*
		 * A bit of delay for synchronization... 
		 */
		
		long time = System.currentTimeMillis();
		while(System.currentTimeMillis() - time <= 1000);
	}
	
	public static ArrayList <Integer> readAnswerAndRemoveTOUR_FILE() throws IOException {
		Scanner in = new Scanner(new FileReader(new File(TOUR_FILE)));
		ArrayList <Integer> permutation = new ArrayList <> ();
		while(true) 
			if(in.nextLine().equals("TOUR_SECTION"))
				break;
		while(true) {
			int pi = Integer.parseInt(in.next());
			if(pi == -1)
				break;
			
			/*
			 * The answer is 1-indexed
			 */
			
			permutation.add(pi - 1);
		}
		//Runtime.getRuntime().exec("del " + TOUR_FILE);
		return permutation;
	}
	
	public static ArrayList <Integer> getPermutation(double[][] distances) throws IOException {
		writePARAMETER_FILE();
		writePROBLEM_FILE(distances);
		execute();
		return readAnswerAndRemoveTOUR_FILE();
	}
}

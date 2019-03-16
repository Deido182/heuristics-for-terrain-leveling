import java.io.IOException;

public class GRASP_Solver implements Solver {
	
	Field field;
	Truck truck;
	final int times1 = 10;
	final int times2 = 100;
	
	public GRASP_Solver(Field field, Truck truck) {
		this.field = field;
		this.truck = truck;
	}
	
	public Path solve() throws IOException, InterruptedException {
		
	}
}

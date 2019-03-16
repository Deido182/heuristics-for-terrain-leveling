import java.util.ArrayList;

public class NearestNeighbourChainsBuilder implements ChainsBuilder {

	Field field;
	Truck truck;
	
	public NearestNeighbourChainsBuilder(Field field, Truck truck) {
		this.field = field;
		this.truck = truck;
	}
	
	public Path getChainOfPeaks(Coordinates from, long quantity) {
		return new GRASP_ChainsBuilder(field, truck, 0.0, 1).getChainOfPeaks(from, quantity);
	}

	public ArrayList<Path> getAllChainsOfPeaks(Coordinates from) {
		return new GRASP_ChainsBuilder(field, truck, 0.0, 1).getAllChainsOfPeaks(from);
	}

	public Path getChainOfHoles(Coordinates from, long quantity) {
		return new GRASP_ChainsBuilder(field, truck, 0.0, 1).getChainOfHoles(from, quantity);
	}

	public ArrayList<Path> getAllChainsOfHoles(Coordinates from) {
		return new GRASP_ChainsBuilder(field, truck, 0.0, 1).getAllChainsOfHoles(from);
	}
	
	public void fixField() {
		new GRASP_ChainsBuilder(field, truck, 0.0, 1).fixField();
	}
}

import java.util.ArrayList;

public class NearestNeighbourChainsBuilder implements ChainsBuilder {

	Field field;
	Truck truck;
	
	public NearestNeighbourChainsBuilder(Field field, Truck truck) {
		this.field = field;
		this.truck = truck;
	}
	
	public Truck getChainOfPeaks(Truck lastTruck, long quantity) {
		return new GRASP_ChainsBuilder(field, truck, 0.0, 1).getChainOfPeaks(lastTruck, quantity);
	}

	public ArrayList<Truck> getAllChainsOfPeaks() {
		return new GRASP_ChainsBuilder(field, truck, 0.0, 1).getAllChainsOfPeaks();
	}

	public Truck getChainOfHoles(Truck lastTruck, long quantity) {
		return new GRASP_ChainsBuilder(field, truck, 0.0, 1).getChainOfHoles(lastTruck, quantity);
	}

	public ArrayList<Truck> getAllChainsOfHoles() {
		return new GRASP_ChainsBuilder(field, truck, 0.0, 1).getAllChainsOfHoles();
	}
	
	public void fixField() {
		new GRASP_ChainsBuilder(field, truck, 0.0, 1).fixField();
	}
}

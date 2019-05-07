import java.util.ArrayList;

public interface ChainsBuilder {
	public Truck getChainOfPeaks(Truck lastTruck, long quantity);
	public ArrayList <Truck> getAllChainsOfPeaks();
	public Truck getChainOfHoles(Truck lastTruck, long quantity);
	public ArrayList <Truck> getAllChainsOfHoles();
	public void fixField();
}

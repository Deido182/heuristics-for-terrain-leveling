import java.util.ArrayList;

public interface ChainsBuilder {
	public Path getChainOfPeaks(Coordinates from, long quantity);
	public ArrayList <Path> getAllChainsOfPeaks(Coordinates from);
	public Path getChainOfHoles(Coordinates from, long quantity);
	public ArrayList <Path> getAllChainsOfHoles(Coordinates from);
	public void fixField();
}

import java.util.ArrayList;

public interface ChainsBuilder {
	public Path getChainOfPeaks(Path from, long quantity);
	public ArrayList <Path> getAllChainsOfPeaks(Path from);
	public Path getChainOfHoles(Path from, long quantity);
	public ArrayList <Path> getAllChainsOfHoles(Path from);
	public void fixField();
}

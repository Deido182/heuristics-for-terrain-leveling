import java.util.ArrayList;

public class Path {
	private class Stopover {
		Coordinates coordinates;
		double quantityToBringIn;
		
		public Stopover(Coordinates coordinates, double quantityToBringIn) {
			this.coordinates = coordinates;
			this.quantityToBringIn = quantityToBringIn;
		}
	}
	
	ArrayList <Stopover> stopovers;
	
	public Path() {
		stopovers = new ArrayList <> ();
	}
	
	public int length() {
		return stopovers.size();
	}
	
	public Stopover remove(int i) {
		return stopovers.remove(i);
	}
	
	private void addStopover(int i, Stopover s) {
		stopovers.add(i, s);
	}
	
	private void addStopover(Stopover s) {
		addStopover(length(), s);
	}
	
	public void addStopover(int i, Coordinates c, double q) {
		addStopover(i, new Stopover(c, q));
	}
	
	public void addStopover(Coordinates c, double q) {
		addStopover(length(), c, q);
	}
	
	public void addStopover(int i, Coordinates c) {
		Stopover removed = remove(i + 1);
		addStopover(i + 1, c, removed.quantityToBringIn);
		addStopover(i + 2, removed);
	}
	
	public void addTwoStopovers(int i, Coordinates c1, Coordinates c2) {
		addStopover(i, c1);
		addStopover(i + 1, c2);
	}
	
	public void append(Path p) {
		for(Stopover s : p.stopovers)
			addStopover(s);
	}
	
	public Movement getMovement(int i) {
		return new Movement(stopovers.get(i).coordinates, stopovers.get(i + 1).coordinates, stopovers.get(i + 1).quantityToBringIn);
	}
	
	public Movement getLastMovement() {
		return getMovement(length() - 2);
	}
}

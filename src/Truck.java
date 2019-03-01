import java.io.IOException;
import java.util.ArrayList;

public class Truck {
	double capacity;
	double minimumMove;
	Path path;
	
	public Truck(double capacity, double minimumMove, Coordinates from) {
		this.capacity = capacity;
		this.minimumMove = minimumMove;
		this.path = new Path();
		path.addStopover(from, 0.0);
	}
	
	public Coordinates getCurrentPosition() {
		return path.getLastCoordinates();
	}
	
	public void move(Coordinates to, double quantity) throws IOException {
		path.addStopover(to, quantity);
	}
	
	public void move(Coordinates to) throws IOException {
		move(to, 0.0);
	}
	
	public void move(Path p) {
		path.append(p);
	}
	
	public Movement getMovement(int i) {
		return new Movement(path.getCoordinates(i), path.getCoordinates(i + 1), path.getQuantityToBringIn(i + 1));
	}
	
	public Movement getLastMovement() {
		return getMovement(path.length() - 2);
	}
	
	public void removeMovement(int i) {
		path.remove(i + 1);
	}
}

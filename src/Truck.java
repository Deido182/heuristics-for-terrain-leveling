import java.io.IOException;
import java.util.ArrayList;

public class Truck {
	double capacity;
	double minimumMove;
	Coordinates currentPosition;
	ArrayList <Movement> path;
	
	public Truck(double capacity, double minimumMove, Coordinates currentPosition) {
		this.capacity = capacity;
		this.minimumMove = minimumMove;
		this.currentPosition = currentPosition;
		path = new ArrayList <> ();
	}
	
	public void move(Coordinates to, double quantity) throws IOException {
		if(quantity < 0 || quantity > capacity)
			throw new IOException("The required quantity is not valid");
		path.add(new Movement(currentPosition, to, quantity));
		currentPosition = to;
	}
	
	public void move(Coordinates to) throws IOException {
		move(to, 0.0);
	}
	
	public void move(ArrayList <Movement> movements) {
		if(currentPosition != null)
			path.add(new Movement(currentPosition, movements.get(0).from));
		path.addAll(movements);
		currentPosition = getLastMovement().to;
	}
	
	public Movement getMovement(int i) {
		return path.get(i);
	}
	
	public void removeMovement(int i) {
		path.remove(i);
	}
	
	public Movement getLastMovement() {
		return getMovement(path.size() - 1);
	}
	
	public void addStopoverAfter(Coordinates stopover, int i) {
		Movement removed = path.remove(i + 1);
		path.add(i + 1, new Movement(getMovement(i).to, stopover, removed.quantity));
		path.add(i + 2, new Movement(stopover, removed.to, removed.quantity));
	}
	
	public void addStopoversAfter(ArrayList <Coordinates> stopovers, int i) {
		addStopoverAfter(stopovers.get(0), i);
		addStopoverAfter(stopovers.get(1), i + 1);
	}
}


public class Truck {
	
	long capacity;
	Path path;
	
	public Truck(long capacity, Coordinates from, long initialCargo) {
		this.capacity = capacity;
		this.path = new Path();
		path.addStopover(from, initialCargo);
	}
	
	public Coordinates getCurrentPosition() {
		return path.getLastCoordinates();
	}
	
	public void move(Coordinates to, long quantity) {
		path.addStopover(to, quantity);
	}
	
	public void move(Coordinates to) {
		move(to, 0);
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
}

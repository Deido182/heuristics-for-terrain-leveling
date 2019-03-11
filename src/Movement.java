
public class Movement {
	Coordinates from, to;
	long quantity;
	
	public Movement(Coordinates from, Coordinates to, long quantity) {
		this.from = from;
		this.to = to;
		this.quantity = quantity;
	}
	
	public Movement(Coordinates from, Coordinates to) {
		this(from, to, 0);
	}
	
	@Override
	public String toString() {
		return from.toString() + " " + to.toString() + " " + quantity;
	}
}

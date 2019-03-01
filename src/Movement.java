
public class Movement {
	Coordinates from, to;
	double quantity;
	
	public Movement(Coordinates from, Coordinates to, double quantity) {
		this.from = from;
		this.to = to;
		this.quantity = quantity;
	}
	
	public Movement(Coordinates from, Coordinates to) {
		this(from, to, 0.0);
	}
	
	public String toString() {
		return from.toString() + " " + to.toString() + " " + quantity;
	}
}

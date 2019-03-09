
public class Coordinates extends Vector2D {
	
	public Coordinates(double x, double y) {
		super(x, y);
	}
	
	public Coordinates(Coordinates c, Vector2D direction) {
		super(c.add(direction));
	}
	
	/*
	 * Without overriding the hashCode the toString() of field will not work.
	 * This because with the hashCode of Object it checks for equals Objects and NOT for 
	 * equals Coordinates.
	 */
	
	@Override
	public int hashCode() {
		double tmp = (y + ((long)(x + 1) / 2));
		return (int)(x + (tmp * tmp));
	}
	
	public double distance(Coordinates c) {
		return subtract(c).euclideanNorm();
	}
}

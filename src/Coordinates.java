
public class Coordinates extends Vector2D {
	
	public Coordinates(double x, double y) {
		super(x, y);
	}
	
	public Coordinates(Coordinates c, Vector2D direction) {
		super(c.add(direction));
	}
	
	/*
	 * When using HashMaps is always better override hashCode() to speed up 
	 * queries.
	 * 
	 * Moreover... 
	 * 
	 * HashMap <Coordinates> map = new HashMap <> ();
	 * map.add(new Coordinates(x, y), q);
	 * map.containsKey(new Coordinates(x, y)) will return false because the two 
	 * Objects are different (but the coordinates are not).
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

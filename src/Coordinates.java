
public class Coordinates {
	public double x, y;
	public static final double MAX_ERROR_COOR = 1E-6;
	
	public Coordinates(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public boolean hasX(double x) {
		return Math.abs(this.x - x) < MAX_ERROR_COOR;
	}
	
	public boolean sameX(Coordinates c) {
		return hasX(c.x);
	}
	
	public boolean hasY(double y) {
		return Math.abs(this.y - y) < MAX_ERROR_COOR;
	}
	
	public boolean sameY(Coordinates c) {
		return hasY(c.y);
	}
	
	@Override
	public boolean equals(Object o) {
		return sameX((Coordinates)o) && sameY((Coordinates)o);
	}
	
	/*
	 * Without overriding the hashCode the toString() of field will not work.
	 * This because with the hashCode of Object it checks for equals Objects and NOT for 
	 * equals Coordinates.
	 */
	
	@Override
	public int hashCode()
	{
		double tmp = (y + ((long)(x + 1) / 2));
		return (int)(x + (tmp * tmp));
	}
	
	public double distance(Coordinates c) {
		return Math.sqrt((x - c.x) * (x - c.x) + (y - c.y) * (y - c.y));
	}
	
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}

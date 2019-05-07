
public class Vector2D {
	
	public static final double MAX_ERROR_COOR = 1E-6;
	double x, y;
	
	public Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2D(Vector2D v) {
		this(v.x, v.y);
	}
	
	public Vector2D(Movement m) {
		this(m.to.x - m.from.x, m.to.y - m.from.y);
	}
	
	public double scalarProduct(Vector2D v) {
		return x * v.x + y * v.y;
	}
	
	public double euclideanNorm() {
		return Math.sqrt(scalarProduct(this));
	}
	
	public Vector2D multiply(double k) {
		return new Vector2D(k * x, k * y);
	}
	
	public Vector2D divide(double k) {
		return multiply(1.0 / k);
	}
	
	public Vector2D add(Vector2D v) {
		return new Vector2D(x + v.x, y + v.y);
	}
	
	public Vector2D subtract(Vector2D v) {
		return add(new Vector2D(-v.x, -v.y));
	}
	
	public Vector2D setLengthTo(double length) {
		return new Vector2D(x, y).divide(euclideanNorm()).multiply(length);
	}
	
	public Vector2D rotate(double alpha) {
		return new Vector2D(x * Math.cos(alpha) - y * Math.sin(alpha), x * Math.sin(alpha) + y * Math.cos(alpha));
	}
	
	public double getAngle(Vector2D v) {
		double cos = scalarProduct(v) / (euclideanNorm() * v.euclideanNorm());
		
		/*
		 * The approximation of floating point representation could make it 
		 * possible.
		 */
		
		if(cos < -1.0)
			cos = -1.0;
		if(cos > 1.0)
			cos = 1.0;
		
		return Math.acos(cos);
	}
	
	public Vector2D getVectorByAngle(double alpha, double length) {
		return rotate(alpha).setLengthTo(length);
	}
	
	public boolean clockwise(Vector2D v) {
		Vector2D orth = getVectorByAngle(Math.PI / 2.0, 1.0);
		return v.subtract(orth.multiply(-1.0)).euclideanNorm() <= v.subtract(orth).euclideanNorm();
	}
	
	public boolean hasX(double x) {
		return Math.abs(this.x - x) < MAX_ERROR_COOR;
	}
	
	public boolean sameX(Vector2D v) {
		return hasX(v.x);
	}
	
	public boolean hasY(double y) {
		return Math.abs(this.y - y) < MAX_ERROR_COOR;
	}
	
	public boolean sameY(Vector2D v) {
		return hasY(v.y);
	}
	
	@Override
	public boolean equals(Object o) {
		return sameX((Vector2D)o) && sameY((Vector2D)o);
	}
	
	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}

import java.util.ArrayList;

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
	
	public Vector2D add(Vector2D v) {
		return new Vector2D(x + v.x, y + v.y);
	}
	
	public Vector2D subtract(Vector2D v) {
		return add(new Vector2D(-v.x, -v.y));
	}
	
	public Vector2D setLengthTo(double length) {
		return new Vector2D(x / euclideanNorm() * length, y / euclideanNorm() * length);
	}
	
	public Vector2D rotate(double alpha) {
		return new Vector2D(x * Math.cos(alpha) + y * Math.sin(alpha), -x * Math.sin(alpha) + y * Math.cos(alpha));
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
	
	public ArrayList <Vector2D> getVectorsByAngle(double alpha, double length) {
		Vector2D v1 = rotate(alpha).setLengthTo(length);
		Vector2D v2 = rotate(-alpha).setLengthTo(length);
		
		ArrayList <Vector2D> vectors = new ArrayList <> ();
		vectors.add(v1);
		if(!v1.equals(v2))
			vectors.add(v2);
		
		return vectors;
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

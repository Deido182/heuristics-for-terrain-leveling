import java.util.ArrayList;

import static java.lang.Math.pow;
import static java.lang.Math.abs;
import static java.lang.Math.PI;
import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

public class Truck {
	
	long capacity;
	double gamma;
	double S;
	Path path;
	public static final double ACCEPTED_ERROR = 1E-5;
	
	public Truck(long capacity, double maxAngle, double sensibility, Coordinates from, long initialCargo) {
		this.capacity = capacity;
		this.gamma = maxAngle;
		this.S = sensibility;
		this.path = new Path();
		path.addStopover(from, initialCargo);
	}
	
	private Truck(long capacity, double maxAngle, double sensibility, Path path) {
		this.capacity = capacity;
		this.gamma = maxAngle;
		this.S = sensibility;
		this.path = path.clone();
	}
	
	public Truck clone() {
		return new Truck(capacity, gamma, S, path);
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
		return getMovement(path.size() - 2);
	}
	
	/**
	 * Returns the angle executed by the truck (arrived to c2 from c1) to go to c3.
	 * 
	 * @param c1
	 * @param c2
	 * @param c3
	 * @return the angle.
	 */
	
	public static double getAngle(Coordinates c1, Coordinates c2, Coordinates c3) {
		return c2.subtract(c1).getAngle(c3.subtract(c2));
	}
	
	/**
	 * Checks the angle. It should be between 0 and gamma (inclusive).
	 * The "ACCEPTED_ERROR" is for the floating point approximation.
	 * 
	 * @param angle
	 * @return True if the angle is acceptable. False otherwise.
	 */
	
	public boolean angleOk(double angle) {
		return abs(angle) <= gamma + ACCEPTED_ERROR;
	}
	
	/**
	 * Checks the movement length. It should be >= S.
	 * The "ACCEPTED_ERROR" is for the floating point approximation.
	 * 
	 * @param c1
	 * @param c2
	 * @return True if the length is acceptable. False otherwise.
	 */
	
	public boolean movementOk(Coordinates c1, Coordinates c2) {
		return c1.distance(c2) >= S - ACCEPTED_ERROR;
	}
	
	/**
	 * Given the position i of the coordinates for which the angle abc is not valid, 
	 * fix the path and returns the new path ab...c
	 * 
	 * @param absAlpha
	 * @param i
	 * @return the path a -> c
	 */
	
	public Path insertRegularPolygon(double absAlpha, int i) {
		Stopover sa = path.stopovers.get(i - 2);
		Stopover sb = path.stopovers.get(i - 1);
		Stopover sc = path.stopovers.get(i);
		
		Coordinates a = sa.coordinates;
		Coordinates b = sb.coordinates;
		Coordinates c = sc.coordinates;
		
		final Vector2D C = c.subtract(b); // for the last phase
		
		boolean clockwise = b.subtract(a).clockwise(c.subtract(a));
		
		double N = ceil(2 * PI / gamma);
		double angle = 2 * PI / N;
		
		double beta = clockwise ? -absAlpha + (PI - angle) : absAlpha - (PI - angle);
		
		angle *= clockwise ? -1 : 1;
		
		if(!angleOk(beta)) {
			beta = (clockwise ? 1 : -1) * angle;
			angle *= -1;
		}
		
		Vector2D dir = b.subtract(a).getVectorByAngle(beta, S);
		Coordinates stopover = new Coordinates(b, dir);
		path.addStopover(i, stopover, sc.quantityToBringIn);
		
		int j;
		for(j = i + 1; ; j ++) {
			sa = path.stopovers.get(j - 2);
			sb = path.stopovers.get(j - 1);
			sc = path.stopovers.get(j);
			
			a = sa.coordinates;
			b = sb.coordinates;
			c = sc.coordinates;
			
			if(angleOk(getAngle(a, b, c)))
				break;
			
			dir = b.subtract(a).getVectorByAngle(angle, S);
			stopover = new Coordinates(b, dir);
			path.addStopover(j, stopover, sc.quantityToBringIn);
		}
		
		if(!movementOk(b, c)) {
			ArrayList <Vector2D> sides = new ArrayList <> ();
			Vector2D sum = new Vector2D(0.0, 0.0);
			for(int z = i; z < j; z ++) {
				Coordinates from = path.getCoordinates(z - 1);
				Coordinates to = path.getCoordinates(z);
				
				Vector2D side = to.subtract(from).divide(S);
				
				sides.add(side);
				sum = sum.add(side);
			}
			double delta = 4 * (pow(sum.scalarProduct(C), 2.0) 
							- pow(sum.euclideanNorm(), 2.0) * (pow(C.euclideanNorm(), 2.0) - pow(S, 2.0)));
			double L1 = (2 * sum.scalarProduct(C) - sqrt(delta)) / (2 * pow(sum.euclideanNorm(), 2.0));
			double L2 = (2 * sum.scalarProduct(C) + sqrt(delta)) / (2 * pow(sum.euclideanNorm(), 2.0));
			double L = L1 >= S ? L1 : L2;
			
			for(int z = i; z < j; z ++) {
				Coordinates from = path.getCoordinates(z - 1);
				
				Vector2D side = sides.get(z - i);
				
				path.stopovers.get(z).coordinates = new Coordinates(from, side.multiply(L));
			}
		}
		return path.subPath(i - 2, j + 1);
	}
	
	/**
	 * For each change it checks if it is acceptable and in case it fixes it.
	 */
	
	public void fixPath() {
		for(int i = 2; i < path.size(); i ++) {
			double absAlpha = getAngle(path.getCoordinates(i - 2), path.getCoordinates(i - 1), path.getCoordinates(i));
			
			if(angleOk(absAlpha))
				continue;
			
			// Let's build a regular polygon
			
			insertRegularPolygon(absAlpha, i);
		}
	}
	
	public double distance(Path p) {
		if(path.getLastCoordinates().equals(p.getFirstCoordinates()))
			return 0.0;
		if(path.size() == 1)
			return path.getLastCoordinates().distance(p.getFirstCoordinates());
		
		Truck t = clone();
		t.path = path.clone();
		t.path.append(p.clone());
		
		Stopover sa = t.path.stopovers.get(path.size() - 2);
		Stopover sb = t.path.stopovers.get(path.size() - 1);
		Stopover sc = t.path.stopovers.get(path.size());
		
		Coordinates a = sa.coordinates;
		Coordinates b = sb.coordinates;
		Coordinates c = sc.coordinates;
		
		double absAlpha = Truck.getAngle(a, b, c);
		if(t.angleOk(absAlpha))
			return b.distance(c); // No corrections needed
		
		return t.insertRegularPolygon(absAlpha, path.size()).suffix(1).distance();
	}
	
	public double distance(Coordinates c) {
		return distance(new Path(c));
	}
	
	/**
	 * Tries every pair (chain of peaks, chain of peaks), (chain of holes, chain of holes) and 
	 * swaps them if (and only if) it improves the path.
	 * 
	 * It does not arrive to local minimum because we prefer a guarantee about the time 
	 * required. However going on "usually" does not produce a significant improvement.
	 * 
	 * @param chainsOfPeaks
	 * @param chainsOfHoles
	 */
	
	public void improveSequenceOfChains(ArrayList <Truck> chains) {
		for(int i = 0; i < chains.size() - 1; i ++) {
			for(int j = i + 1; j < chains.size(); j ++) {
				if(i % 2 != j % 2) // we can't swap peaks with holes
					continue;
				
				double cost = 0.0;
				if(i == 0) {
					cost -= distance(chains.get(i).path);
					cost += distance(chains.get(j).path);
				} else {
					cost -= chains.get(i - 1).distance(chains.get(i).path);
					cost += chains.get(i - 1).distance(chains.get(j).path);
				}
				cost -= chains.get(j - 1).distance(chains.get(j).path);
				cost += chains.get(j - 1).distance(chains.get(i).path);
				cost -= chains.get(i).distance(chains.get(i + 1).path);
				cost += chains.get(j).distance(chains.get(i + 1).path);
				if(j < chains.size() - 1) {
					cost -= chains.get(j).distance(chains.get(j + 1).path);
					cost += chains.get(i).distance(chains.get(j + 1).path);
				}
				
				if(cost >= 0.0)
					continue;
				
				Truck chain = chains.get(i);
				chains.set(i, chains.get(j));
				chains.set(j, chain);
			}
		}
	}
	
	public Coordinates getTheNearest(Field f, CellProperty p) {
		Coordinates nearest = null;
		for(Coordinates c : f.cells.keySet()) {
			if(!p.is(c))
				continue;
			if(nearest == null)
				nearest = c;
			else if(distance(c) < distance(nearest))
				nearest = c;
		}
		return nearest;
	}

	public static interface CellProperty {
		public boolean is(Coordinates c);
	}
	
	public Coordinates getTheNearestHole(Field f) {
		return getTheNearest(f, (Coordinates c) -> f.isAnHole(c));
	}
	
	public Coordinates getTheNearestPeak(Field f) {
		return getTheNearest(f, (Coordinates c) -> f.isAPeak(c));
	}
	
	public Coordinates getTheNearestPeakDifferentFromThese(Field f, Coordinates...these) {
		long[] q = new long[these.length];
		for(int i = 0; i < these.length; i ++) 
			if(these[i] != null)
				f.decrement(these[i], q[i] = f.getQuantity(these[i]));
		Coordinates nearest = getTheNearestPeak(f);
		for(int i = 0; i < these.length; i ++) 
			if(these[i] != null)
				f.increment(these[i], q[i]);
		return nearest;
	}
	
	public Coordinates getTheNearestHoleDifferentFromThese(Field f, Coordinates...these) {
		long[] q = new long[these.length];
		for(int i = 0; i < these.length; i ++) 
			if(these[i] != null)
				f.decrement(these[i], q[i] = f.getQuantity(these[i]));
		Coordinates nearest = getTheNearestHole(f);
		for(int i = 0; i < these.length; i ++) 
			if(these[i] != null)
				f.increment(these[i], q[i]);
		return nearest;
	}
	
	public Coordinates getTheMostDistant(Field f, CellProperty p) {
		Coordinates mostDistant = null;
		for(Coordinates c : f.cells.keySet()) {
			if(!p.is(c))
				continue;
			if(mostDistant == null)
				mostDistant = c;
			else if(distance(c) > distance(mostDistant))
				mostDistant = c;
		}
		return mostDistant;
	}
	
	public Coordinates getTheMostDistantHole(Field f) {
		return getTheMostDistant(f, (Coordinates c) -> f.isAnHole(c));
	}
	
	public Coordinates getTheMostDistantPeak(Field f) {
		return getTheMostDistant(f, (Coordinates c) -> f.isAPeak(c));
	}
	
	public Coordinates getTheNearestOfTheSameTypeDifferent(Field f) {
		if(f.isAnHole(path.getLastCoordinates()))
			return getTheNearestHoleDifferentFromThese(f, path.getLastCoordinates());
		if(f.isAPeak(path.getLastCoordinates()))
			return getTheNearestPeakDifferentFromThese(f, path.getLastCoordinates());
		return null;
	}
	
	public Coordinates getTheMostDistantOfTheSameType(Field f) {
		if(f.isAnHole(path.getLastCoordinates()))
			return getTheMostDistantHole(f);
		if(f.isAPeak(path.getLastCoordinates()))
			return getTheMostDistantPeak(f);
		return null;
	}
}

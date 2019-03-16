import java.util.ArrayList;

public class Truck {
	
	long capacity;
	Path path;
	public static final double ACCEPTED_ERROR = 1E-5;
	
	public Truck(long capacity, Coordinates from, long initialCargo) {
		this.capacity = capacity;
		this.path = new Path();
		path.addStopover(from, initialCargo);
	}
	
	private Truck(long capacity, Path path) {
		this.capacity = capacity;
		this.path = path.clone();
	}
	
	public Truck clone() {
		return new Truck(capacity, path);
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
	 * Checks the angle. It should be between 0 and PI/2 (inclusive).
	 * The "ACCEPTED_ERROR" is for the floating point approximation.
	 * 
	 * @param angle
	 * @return True if the angle is acceptable. False otherwise.
	 */
	
	public static boolean isOk(double angle) {
		return angle <= Math.PI / 2 + ACCEPTED_ERROR;
	}
	
	/**
	 * Returns the stopover to add after c2 to fix the angle if a single stopover is enough 
	 * to solve it. Returns null otherwise.
	 * 
	 * @param c1
	 * @param c2
	 * @param c3
	 * @return the stopover (coordinates) to add after c2 or null.
	 */
	
	private Coordinates singleStopover(Coordinates c1, Coordinates c2, Coordinates c3, final double LENGTH) {
		double angle = getAngle(c1, c2, c3);
		if(angle > 0.75 * Math.PI)
			return null;
		Vector2D v = c3.subtract(c2);
		for(Vector2D dir : v.getVectorsByAngle(angle - Math.PI / 2, LENGTH)) {
			Coordinates s = new Coordinates(c2, dir);
			
			if(!isOk(getAngle(c1, c2, s))) // turn on the other side
				continue;
			if(!isOk(getAngle(c2, s, c3))) 
				continue;
			
			return s;
		}
		return null;
	}
	
	/**
	 * Returns two stopovers to add after c2 (in the given order) to fix the angle.
	 * 
	 * @param c1
	 * @param c2
	 * @param c3
	 * @return two stopovers (coordinates) to add (in order) after c2.
	 */
	
	private ArrayList <Coordinates> twoStopovers(Coordinates c1, Coordinates c2, Coordinates c3, final double LENGTH) {
		double angle = getAngle(c1, c2, c3);
		Vector2D v = c3.subtract(c2);
		for(Vector2D dir1 : v.getVectorsByAngle(angle - Math.PI / 2, LENGTH)) {
			Coordinates s1 = new Coordinates(c2, dir1);
			
			if(!isOk(getAngle(c1, c2, s1))) // turn on the other side
				continue;
			
			Coordinates s2 = new Coordinates(s1, v.setLengthTo(LENGTH));
			
			if(!isOk(getAngle(s1, s2, c3))) 
				continue;
			
			ArrayList <Coordinates> stopovers = new ArrayList <> ();
			stopovers.add(s1);
			stopovers.add(s2);
			return stopovers;
		}
		return null;
	}
	
	/**
	 * For each change it checks if it is acceptable and in case it fixes it.
	 */
	
	public void fixPath(final double LENGTH) {
		for(int i = 2; i < path.length(); i ++) {
			double angle = getAngle(path.getCoordinates(i - 2), path.getCoordinates(i - 1), path.getCoordinates(i));
			
			if(isOk(angle))
				continue;
			
			Coordinates stopover = singleStopover(path.getCoordinates(i - 2), path.getCoordinates(i - 1), path.getCoordinates(i), LENGTH);
			if(stopover != null) {
				path.rerouteOne(i - 1, stopover);
				continue;
			}
			
			ArrayList <Coordinates> stopovers = twoStopovers(path.getCoordinates(i - 2), path.getCoordinates(i - 1), path.getCoordinates(i), LENGTH);
			path.rerouteTwo(i - 1, stopovers.get(0), stopovers.get(1));
		}
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
	
	public void improveSequenceOfChains(ArrayList <Path> chains) {
		for(int i = 0; i < chains.size() - 1; i ++) {
			for(int j = i + 1; j < chains.size(); j ++) {
				if(i % 2 != j % 2) // we can't swap peaks with holes
					continue;
				
				double cost = 0.0;
				if(i == 0) {
					cost -= path.getLastCoordinates().distance(chains.get(i).getFirstCoordinates());
					cost += path.getLastCoordinates().distance(chains.get(j).getFirstCoordinates());
				} else {
					cost -= chains.get(i - 1).getLastCoordinates().distance(chains.get(i).getFirstCoordinates());
					cost += chains.get(i - 1).getLastCoordinates().distance(chains.get(j).getFirstCoordinates());
				}
				cost -= chains.get(j - 1).getLastCoordinates().distance(chains.get(j).getFirstCoordinates());
				cost += chains.get(j - 1).getLastCoordinates().distance(chains.get(i).getFirstCoordinates());
				cost -= chains.get(i).getLastCoordinates().distance(chains.get(i + 1).getFirstCoordinates());
				cost += chains.get(j).getLastCoordinates().distance(chains.get(i + 1).getFirstCoordinates());
				if(j < chains.size() - 1) {
					cost -= chains.get(j).getLastCoordinates().distance(chains.get(j + 1).getFirstCoordinates());
					cost += chains.get(i).getLastCoordinates().distance(chains.get(j + 1).getFirstCoordinates());
				}
				
				if(cost >= 0.0)
					continue;
				
				Path chain = chains.get(i);
				chains.set(i, chains.get(j));
				chains.set(j, chain);
			}
		}
	}
}

import java.util.ArrayList;

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
	 * Checks the angle. It should be between 0 and gamma (inclusive).
	 * The "ACCEPTED_ERROR" is for the floating point approximation.
	 * 
	 * @param angle
	 * @return True if the angle is acceptable. False otherwise.
	 */
	
	public boolean angleOk(double angle) {
		return Math.abs(angle) <= gamma + ACCEPTED_ERROR;
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
	 * For each change it checks if it is acceptable and in case it fixes it.
	 */
	
	public void fixPath() {
		for(int i = 2; i < path.length(); i ++) {
			double absAlpha = getAngle(path.getCoordinates(i - 2), path.getCoordinates(i - 1), path.getCoordinates(i));
			
			if(angleOk(absAlpha))
				continue;
			
			// Let's build a regular polygon
			
			Stopover sa = path.stopovers.get(i - 2);
			Stopover sb = path.stopovers.get(i - 1);
			Stopover sc = path.stopovers.get(i);
			
			Coordinates a = sa.coordinates;
			Coordinates b = sb.coordinates;
			Coordinates c = sc.coordinates;
			
			boolean clockwise = b.subtract(a).clockwise(c.subtract(a));
			
			double N = Math.ceil(2 * Math.PI / gamma);
			double angle = 2 * Math.PI / N;
			
			double beta = clockwise ? -absAlpha + (Math.PI - angle) : absAlpha - (Math.PI - angle);
			
			angle *= clockwise ? -1 : 1;
			
			if(!angleOk(beta)) {
				beta = (clockwise ? 1 : -1) * gamma;
				angle *= -1;
			}
			
			Vector2D dir = b.subtract(a).getVectorByAngle(beta, S);
			Coordinates stopover = new Coordinates(b, dir);
			path.addStopover(i, stopover, sc.quantityToBringIn);
			
			for(int j = i + 1; ; j ++) {
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

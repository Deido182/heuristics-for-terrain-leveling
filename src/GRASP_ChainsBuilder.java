import java.util.ArrayList;
import java.util.Random;

public class GRASP_ChainsBuilder implements ChainsBuilder {
	
	Field field;
	Truck truck;
	private double alpha;
	private int choices;
	
	public GRASP_ChainsBuilder(Field field, Truck truck, double alpha, int choices) {
		this.field = field;
		this.truck = truck;
		this.alpha = alpha;
		this.choices = choices;
	}
	
	public double getGRASP_Threshold(Coordinates pos) {
		double min = pos.distance(field.getTheNearestOfTheSameTypeDifferent(pos));
		double max = pos.distance(field.getTheMostDistantOfTheSameType(pos));
		
		return min + alpha * (max - min);
	}
	
	/**
	 * Creates a path of peaks to collect the required quantity of terrain.
	 * 
	 * @param from the coordinates from which search the first peak.
	 * @param quantity the quantity of terrain we want to leave at the end of the chain.
	 * @return the path.
	 */
	
	public Path getChainOfPeaks(Coordinates from, long quantity) {
		Truck newTruck = new Truck(quantity, field.getTheNearestPeak(from), 0);
		newTruck.move(newTruck.getCurrentPosition()); // just to add at least a movement
		while(field.getQuantity(newTruck.getCurrentPosition()) < newTruck.capacity) {
			Coordinates[] nextPeaks = new Coordinates[choices];
			nextPeaks[0] = field.getTheNearestPeakDifferentFromThese(newTruck.getCurrentPosition(), newTruck.getCurrentPosition());
			nextPeaks[1] = field.getTheNearestPeakDifferentFromThese(newTruck.getCurrentPosition(), newTruck.getCurrentPosition(), nextPeaks[0]);
			nextPeaks[2] = field.getTheNearestPeakDifferentFromThese(newTruck.getCurrentPosition(), newTruck.getCurrentPosition(), nextPeaks[0], nextPeaks[1]);
			double GRASP_Threshold = getGRASP_Threshold(newTruck.getCurrentPosition());
			int ok = choices;
			while(ok > 1) { // never less then 1
				if(nextPeaks[ok - 1] != null)
					if(newTruck.getCurrentPosition().distance(nextPeaks[ok - 1]) <= GRASP_Threshold)
						break;
				ok --;
			}
			newTruck.move(nextPeaks[new Random().nextInt(ok)], field.getQuantity(newTruck.getCurrentPosition()));
			field.update(newTruck.getLastMovement());
		}
		field.decrement(newTruck.getCurrentPosition(), newTruck.capacity);
		return newTruck.path;
	}
	
	/**
	 * Gets all the chains of peaks.
	 * 
	 * @param from the coordinates from which search the first chain of peaks.
	 * @return an ArrayList of paths
	 */
	
	public ArrayList <Path> getAllChainsOfPeaks(Coordinates from) {
		ArrayList <Path> chainsOfPeaks = new ArrayList <> ();
		Coordinates nextPeak = field.getTheNearestHole(from);
		while(nextPeak != null) {
			Path chain = getChainOfPeaks(nextPeak, truck.capacity);
			chainsOfPeaks.add(chain);
			nextPeak = field.getTheNearestPeak(chain.getLastCoordinates());
		}
		return chainsOfPeaks;
	}
	
	/**
	 * Creates a path of holes to spread the required quantity of terrain.
	 * 
	 * @param from the coordinates from which search the first hole.
	 * @param quantity the quantity of terrain we want to spread along the chain.
	 * @return the path.
	 */
	
	public Path getChainOfHoles(Coordinates from, long quantity) {
		/*
		 * PAY ATTENTION: the truck has to bring in the first cell "quantity" units of terrain 
		 * to fill the chain of holes.
		 */
		
		Truck newTruck = new Truck(quantity, field.getTheNearestHole(from), quantity);
		field.increment(newTruck.getCurrentPosition(), newTruck.capacity);
		while(field.getQuantity(newTruck.getCurrentPosition()) > 0) {
			Coordinates[] nextHoles = new Coordinates[choices];
			nextHoles[0] = field.getTheNearestHoleDifferentFromThese(newTruck.getCurrentPosition(), newTruck.getCurrentPosition());
			nextHoles[1] = field.getTheNearestHoleDifferentFromThese(newTruck.getCurrentPosition(), newTruck.getCurrentPosition(), nextHoles[0]);
			nextHoles[2] = field.getTheNearestHoleDifferentFromThese(newTruck.getCurrentPosition(), newTruck.getCurrentPosition(), nextHoles[0], nextHoles[1]);
			double GRASP_Threshold = getGRASP_Threshold(newTruck.getCurrentPosition());
			int ok = choices;
			while(ok > 1) { // never less then 1
				if(nextHoles[ok - 1] != null)
					if(newTruck.getCurrentPosition().distance(nextHoles[ok - 1]) <= GRASP_Threshold)
						break;
				ok --;
			}
			newTruck.move(nextHoles[new Random().nextInt(ok)], field.getQuantity(newTruck.getCurrentPosition()));
			field.update(newTruck.getLastMovement());
		}
		return newTruck.path;
	}
	
	/**
	 * Gets all the chains of peaks.
	 * 
	 * @param from the coordinates from which search the first chain of peaks.
	 * @return an ArrayList of paths.
	 */
	
	public ArrayList <Path> getAllChainsOfHoles(Coordinates from) {
		ArrayList <Path> chainsOfHoles = new ArrayList <> ();
		Coordinates nextHole = field.getTheNearestHole(from);
		while(nextHole != null) {
			Path chain = getChainOfHoles(nextHole, truck.capacity);
			chainsOfHoles.add(chain);
			nextHole = field.getTheNearestHole(chain.getLastCoordinates());
		}
		return chainsOfHoles;
	}
}

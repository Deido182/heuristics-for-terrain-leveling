import java.util.ArrayList;
import java.util.Random;

public class GRASP_ChainsBuilder implements ChainsBuilder {
	
	Field field;
	Truck truckModel;
	double alpha;
	int choices;
	
	public GRASP_ChainsBuilder(Field field, Truck truck, double alpha, int choices) {
		this.field = field;
		this.truckModel = truck;
		this.alpha = alpha;
		this.choices = choices;
	}
	
	public static double getGRASP_Threshold(final Truck truck, final Field field, double alpha) {
		Coordinates nearest = truck.getTheNearestOfTheSameTypeDifferent(field);
		if(nearest == null)
			nearest = truck.getCurrentPosition();
		Coordinates mostDistant = truck.getTheMostDistantOfTheSameType(field);
		
		double min = truck.distance(nearest);
		double max = truck.distance(mostDistant);
		
		return min + alpha * (max - min);
	}
	
	/**
	 * Creates a path of peaks to collect the required quantity of terrain.
	 * 
	 * @param from the coordinates from which search the first peak.
	 * @param quantity the quantity of terrain we want to leave at the end of the chain.
	 * @return the path.
	 */
	
	public Truck getChainOfPeaks(final Truck lastTruck, long quantity) {
		Coordinates from = lastTruck.getTheNearestPeak(field);
		if(from == null)
			return null;
		Truck truck = new Truck(quantity, truckModel.gamma, truckModel.S, from, 0);
		truck.move(truck.getCurrentPosition()); // just to add at least a movement
		while(field.getQuantity(truck.getCurrentPosition()) < truck.capacity) {
			Coordinates[] toAvoid = new Coordinates[choices + 1];
			toAvoid[0] = truck.getCurrentPosition();
			Coordinates[] nextPeaks = new Coordinates[choices];
			for(int i = 0; i < choices; i ++) 
				toAvoid[i + 1] = nextPeaks[i] = truck.getTheNearestPeakDifferentFromThese(field, toAvoid);
			double GRASP_Threshold = getGRASP_Threshold(truck, field, alpha);
			int ok = choices;
			while(ok > 1) { // never less then 1
				if(nextPeaks[ok - 1] != null)
					if(truck.distance(nextPeaks[ok - 1]) <= GRASP_Threshold)
						break;
				ok --;
			}
			truck.move(nextPeaks[new Random().nextInt(ok)], field.getQuantity(truck.getCurrentPosition()));
			field.update(truck.getLastMovement());
		}
		field.decrement(truck.getCurrentPosition(), truck.capacity);
		return truck;
	}
	
	/**
	 * Gets all the chains of peaks.
	 * 
	 * @param from the coordinates from which search the first chain of peaks.
	 * @return an ArrayList of paths
	 */
	
	public ArrayList <Truck> getAllChainsOfPeaks() {
		Truck lastTruck = truckModel.clone();
		ArrayList <Truck> chainsOfPeaks = new ArrayList <> ();
		while((lastTruck = getChainOfPeaks(lastTruck, truckModel.capacity)) != null) 
			chainsOfPeaks.add(lastTruck);
		return chainsOfPeaks;
	}
	
	/**
	 * Creates a path of holes to spread the required quantity of terrain.
	 * 
	 * @param from the coordinates from which search the first hole.
	 * @param quantity the quantity of terrain we want to spread along the chain.
	 * @return the path.
	 */
	
	public Truck getChainOfHoles(final Truck lastTruck, long quantity) {
		/*
		 * PAY ATTENTION: the truck has to bring in the first cell "quantity" units of terrain 
		 * to fill the chain of holes.
		 */
		Coordinates from = lastTruck.getTheNearestHole(field);
		if(from == null)
			return null;
		Truck truck = new Truck(quantity, truckModel.gamma, truckModel.S, from, quantity);
		field.increment(truck.getCurrentPosition(), truck.capacity);
		while(field.getQuantity(truck.getCurrentPosition()) > 0) {
			Coordinates[] toAvoid = new Coordinates[choices + 1];
			toAvoid[0] = truck.getCurrentPosition();
			Coordinates[] nextHoles = new Coordinates[choices];
			for(int i = 0; i < choices; i ++)
				toAvoid[i + 1] = nextHoles[i] = truck.getTheNearestHoleDifferentFromThese(field, toAvoid);
			double GRASP_Threshold = getGRASP_Threshold(truck, field, alpha);
			int ok = choices;
			while(ok > 1) { // never less then 1
				if(nextHoles[ok - 1] != null)
					if(truck.distance(nextHoles[ok - 1]) <= GRASP_Threshold)
						break;
				ok --;
			}
			truck.move(nextHoles[new Random().nextInt(ok)], field.getQuantity(truck.getCurrentPosition()));
			field.update(truck.getLastMovement());
		}
		return truck;
	}
	
	/**
	 * Gets all the chains of peaks.
	 * 
	 * @param from the coordinates from which search the first chain of peaks.
	 * @return an ArrayList of paths.
	 */
	
	public ArrayList <Truck> getAllChainsOfHoles() {
		Truck lastTruck = truckModel.clone();
		ArrayList <Truck> chainsOfHoles = new ArrayList <> ();
		while((lastTruck = getChainOfHoles(lastTruck, truckModel.capacity)) != null) 
			chainsOfHoles.add(lastTruck);
		return chainsOfHoles;
	}
	
	/**
	 * Moves the minimum quantity of terrain necessary to have 
	 * "field.terrainToMove()" multiple of "truck.capacity".
	 */
	
	public void fixField() {
		long remainder = field.terrainToMove() % truckModel.capacity;
		if(remainder == 0)
			return;
		Truck chainOfPeaks = getChainOfPeaks(truckModel, remainder);
		Truck chainOfHoles = getChainOfHoles(chainOfPeaks, remainder);
		truckModel.move(chainOfPeaks.path);
		truckModel.move(chainOfHoles.path);
	}
}

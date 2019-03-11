import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

public class Field {
	
	/*
	 * PROBLEM: try more THRESHOLD value. The problem arises with "getChainOfPeaks" method.
	 * With a value too high we could find a "peak" and start a new chain when actually there is not enough terrain 
	 * to complete it.
	 * 
	 * Two ways to solve it: try with another constant or add an if to the method to check if 
	 * the available terrain is "around" (Math.abs(... - truck.capacity) <= 1E-...) the required quantity.
	 */
	
	public HashMap <Coordinates, Double> cells;
	public double deltaX, deltaY;
	public static final double MAX_ERROR = 1E-3; 
	public static final double THRESHOLD = MAX_ERROR / 20;
	
	public Field(Scanner scanner) {
		cells = new HashMap <> ();
		double mean = 0.0;
		int numberOfCells = Integer.parseInt(scanner.next());
		for(int i = 0; i < numberOfCells; i ++) {
			double x = Double.parseDouble(scanner.next());
			double y = Double.parseDouble(scanner.next()); 
			double q = Double.parseDouble(scanner.next());
			
			cells.put(new Coordinates(x, y), q);
			
			mean += q;
		}
		mean /= numberOfCells;
		
		for(Coordinates c : cells.keySet())
			cells.put(c, cells.get(c) - mean);
		
		deltaX = Double.MAX_VALUE;
		deltaY = Double.MAX_VALUE;
		for(Coordinates c1 : cells.keySet()) {
			for(Coordinates c2 : cells.keySet()) {
				if(!c1.equals(c2) && c1.sameY(c2))
					deltaX = Math.min(deltaX, c1.distance(c2));
				if(!c1.equals(c2) && c1.sameX(c2))
					deltaY = Math.min(deltaY, c1.distance(c2));
			}
		}
	}
	
	public double getQuantity(Coordinates c) {
		return cells.get(c);
	}
	
	public void increment(Coordinates c, double q) {
		if(cells.containsKey(c))
			cells.put(c, cells.get(c) + q);
	}
	
	public void decrement(Coordinates c, double q) {
		increment(c, -q);
	}
	
	public void update(Coordinates from, Coordinates to, double q) {
		decrement(from, q);
		increment(to, q);
	}
	
	public void update(Movement m) {
		update(m.from, m.to, m.quantity);
	}
	
	public boolean isSmooth() {
		double max = 0.0;
		for(Coordinates c : cells.keySet())
			max = Math.max(max, Math.abs(cells.get(c)));
		return max < MAX_ERROR;
	}
	
	public static interface CellProperty {
		public boolean is(Coordinates c);
	}
	
	public boolean isAnHole(Coordinates c) {
		return cells.get(c) <= -THRESHOLD;
	}
	
	public boolean isAPeak(Coordinates c) {
		return cells.get(c) >= THRESHOLD;
	}
	
	public Coordinates getTheNearest(Coordinates from, CellProperty p) {
		Coordinates nearest = null;
		for(Coordinates c : cells.keySet()) {
			if(!p.is(c))
				continue;
			if(nearest == null)
				nearest = c;
			else if(from.distance(c) < from.distance(nearest))
				nearest = c;
		}
		return nearest;
	}
	
	public Coordinates getTheNearestHole(Coordinates from) {
		return getTheNearest(from, (Coordinates c) -> isAnHole(c));
	}
	
	public Coordinates getTheNearestPeak(Coordinates from) {
		return getTheNearest(from, (Coordinates c) -> isAPeak(c));
	}
	
	public Coordinates getTheNearestPeakDifferentFromThisOne(Coordinates from, Coordinates thisOne) {
		double q = getQuantity(thisOne);
		decrement(thisOne, q);
		Coordinates nearest = getTheNearestPeak(from);
		increment(thisOne, q);
		return nearest;
	}
	
	public double terrainToMove() {
		double sum = 0.0;
		for(Coordinates c : cells.keySet())
			if(isAPeak(c))
				sum += getQuantity(c);
		return sum;
	}
}

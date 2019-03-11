import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

public class Field {
	
	public HashMap <Coordinates, Long> cells;
	public double deltaX, deltaY;
	public static final long PRECISION = (int)1E4;
	
	public Field(Scanner scanner) {
		cells = new HashMap <> ();
		long sum = 0;
		int numberOfCells = Integer.parseInt(scanner.next());
		for(int i = 0; i < numberOfCells; i ++) {
			double x = Double.parseDouble(scanner.next());
			double y = Double.parseDouble(scanner.next()); 
			long q = (long)(Double.parseDouble(scanner.next()) * PRECISION);
			
			cells.put(new Coordinates(x, y), q);
			
			sum += q;
		}
		
		long mean = sum / numberOfCells;
		for(Coordinates c : cells.keySet())
			cells.put(c, cells.get(c) - mean);
		
		/*
		 * FIX
		 * 
		 * The error is not relevant so just increment / decrement by one some "random" cells 
		 * until we have a sum = 0 over the field.
		 */
		
		long remainder = sum % numberOfCells;
		long inc = Long.signum(remainder);
		for(Coordinates c : cells.keySet()) {
			if(remainder == 0)
				break;
			cells.put(c, cells.get(c) - inc);
			remainder -= inc;
		}
		
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
	
	public long getQuantity(Coordinates c) {
		return cells.get(c);
	}
	
	public void increment(Coordinates c, long q) {
		if(cells.containsKey(c))
			cells.put(c, cells.get(c) + q);
	}
	
	public void decrement(Coordinates c, long q) {
		increment(c, -q);
	}
	
	public void update(Coordinates from, Coordinates to, long q) {
		decrement(from, q);
		increment(to, q);
	}
	
	public void update(Movement m) {
		update(m.from, m.to, m.quantity);
	}
	
	public boolean isSmooth() {
		for(Coordinates c : cells.keySet())
			if(getQuantity(c) != 0)
				return false;
		return true;
	}
	
	public static interface CellProperty {
		public boolean is(Coordinates c);
	}
	
	public boolean isAnHole(Coordinates c) {
		return getQuantity(c) < 0;
	}
	
	public boolean isAPeak(Coordinates c) {
		return getQuantity(c) > 0;
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
		long q = getQuantity(thisOne);
		decrement(thisOne, q);
		Coordinates nearest = getTheNearestPeak(from);
		increment(thisOne, q);
		return nearest;
	}
	
	public long terrainToMove() {
		long sum = 0;
		for(Coordinates c : cells.keySet())
			if(isAPeak(c))
				sum += getQuantity(c);
		return sum;
	}
}

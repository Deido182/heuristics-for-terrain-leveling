import java.util.HashMap;
import java.util.Scanner;

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
	
	private Field(HashMap <Coordinates, Long> cells, double deltaX, double deltaY) {
		this.cells = (HashMap<Coordinates, Long>) cells.clone();
		this.deltaX = deltaX;
		this.deltaY = deltaY;
	}
	
	public Field clone() {
		return new Field(cells, deltaX, deltaY);
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
	
	public boolean isAnHole(Coordinates c) {
		return getQuantity(c) < 0;
	}
	
	public boolean isAPeak(Coordinates c) {
		return getQuantity(c) > 0;
	}
	
	public long terrainToMove() {
		long sum = 0;
		for(Coordinates c : cells.keySet())
			if(isAPeak(c))
				sum += getQuantity(c);
		return sum;
	}
}
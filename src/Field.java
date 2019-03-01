import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

public class Field {
	public HashMap <Coordinates, Double> cells;
	public final double MAX_ERROR = 1E-3; 
	public double deltaX, deltaY;
	
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
		for(Coordinates c1 : cells.keySet())
			for(Coordinates c2 : cells.keySet())
				if(!c1.equals(c2) && c1.sameY(c2))
					deltaX = Math.min(deltaX, c1.distance(c2));
		
		deltaY = Double.MAX_VALUE;
		for(Coordinates c1 : cells.keySet())
			for(Coordinates c2 : cells.keySet())
				if(!c1.equals(c2) && c1.sameX(c2))
					deltaY = Math.min(deltaY, c1.distance(c2));
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
	
	public boolean isAnHole(Coordinates c) {
		return cells.get(c) <= -MAX_ERROR / 10;
	}
	
	public boolean isAPeak(Coordinates c) {
		return cells.get(c) >= MAX_ERROR / 10;
	}
	
	public Coordinates getTheNearestHole(Coordinates from) {
		Coordinates nearestHole = null;
		for(Coordinates c : cells.keySet()) {
			if(!isAnHole(c))
				continue;
			if(nearestHole == null)
				nearestHole = c;
			else if(from.distance(c) < from.distance(nearestHole))
				nearestHole = c;
		}
		return nearestHole;
	}
	
	public Coordinates getTheNearestPeak(Coordinates from) {
		Coordinates nearestPeak = null;
		for(Coordinates c : cells.keySet()) {
			if(!isAPeak(c))
				continue;
			if(nearestPeak == null)
				nearestPeak = c;
			else if(from.distance(c) < from.distance(nearestPeak))
				nearestPeak = c;
		}
		return nearestPeak;
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
	
	public boolean contains(Coordinates p) {
		/*
		 * c.y is the center, not the border, so +- deltaY / 2
		 */
		
		double yLowerB = Long.MIN_VALUE;
		for(Coordinates c : cells.keySet()) 
			if(c.y - deltaY / 2 <= p.y)
				yLowerB = Math.max(yLowerB, c.y - deltaY / 2);
		
		double yUpperB = Long.MAX_VALUE;
		for(Coordinates c : cells.keySet()) 
			if(c.y + deltaY / 2 >= p.y)
				yUpperB = Math.min(yUpperB, c.y + deltaY / 2);
		
		/*
		 * Integer.MIN_VALUE is just a limit to check if yLowerB has been changed.
		 * Otherwise find a way for checking "yLowerB == Double.MIN_VALUE".
		 */
		
		if(yLowerB < Integer.MIN_VALUE || yUpperB > Integer.MAX_VALUE)
			return false;
		
		double minXLowerB = Double.MAX_VALUE, maxXLowerB = Double.MIN_VALUE;
		for(Coordinates c : cells.keySet())
			if(c.hasY(yLowerB + deltaY / 2)) {
				minXLowerB = Math.min(minXLowerB, c.x - deltaX / 2);
				maxXLowerB = Math.max(maxXLowerB, c.x + deltaX / 2);
			}
		
		double minXUpperB = Double.MAX_VALUE, maxXUpperB = Double.MIN_VALUE;
		for(Coordinates c : cells.keySet())
			if(c.hasY(yUpperB - deltaY / 2)) {
				minXUpperB = Math.min(minXUpperB, c.x - deltaX / 2);
				maxXUpperB = Math.max(maxXUpperB, c.x + deltaX / 2);
			}
		
		double pos1 = (p.x - minXLowerB) * (yUpperB - yLowerB) - (p.y - yLowerB) * (minXUpperB - minXLowerB);
		double pos2 = (p.x - maxXUpperB) * (yLowerB - yUpperB) - (p.y - yUpperB) * (maxXLowerB - maxXUpperB);
		
		return pos1 <= 0 && pos2 <= 0 || pos1 >= 0 && pos2 >= 0;
	}
	
	public String toString() {
		TreeMap <Double, TreeMap <Double, Double>> grid = new TreeMap <> ();
		for(Coordinates c : cells.keySet()) {
			if(!grid.containsKey(c.y))
				grid.put(c.y, new TreeMap <Double, Double> ());
			grid.get(c.y).put(c.x, getQuantity(c));
		}
		
		StringBuilder field = new StringBuilder();
		for(double y : grid.keySet()) {
			for(double x : grid.get(y).keySet()) {
				Coordinates c = new Coordinates(x, y);
				field.append(String.format("%.3f ", getQuantity(c)));
			}
			field.append("\n");
		}
		return field.toString();
	}
}

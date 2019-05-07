import java.util.ArrayList;
import java.util.List;

class Stopover {
	
	/*
	 * We NEED the quantityToBringIn attribute for fixPath. 
	 * Otherwise we could assume that every time the truck moves 
	 * as much terrain as possible.
	 */
	
	Coordinates coordinates;
	long quantityToBringIn;
	
	public Stopover(Coordinates coordinates, long quantityToBringIn) {
		this.coordinates = coordinates;
		this.quantityToBringIn = quantityToBringIn;
	}
	
	public String toString() {
		return "[ " + coordinates + " " + quantityToBringIn + " ]";
	}
}

public class Path {
	
	ArrayList <Stopover> stopovers;
	
	public Path() {
		stopovers = new ArrayList <> ();
	}
	
	public Path(Coordinates...coordinates) {
		this();
		for(Coordinates c : coordinates)
			stopovers.add(new Stopover(c, 0));
	}
	
	public Path(List<Stopover> stopovers) {
		this.stopovers = new ArrayList <> ();
		this.stopovers.addAll(stopovers);
	}
	
	public Path clone() {
		return new Path(stopovers);
	}
	
	public int size() {
		return stopovers.size();
	}
	
	public Stopover removeStopover(int i) {
		return stopovers.remove(i);
	}
	
	private void addStopover(int i, Stopover s) {
		if(i > 0)
			if(getCoordinates(i - 1).equals(s.coordinates))
				return;
		if(i < size())
			if(s.coordinates.equals(getCoordinates(i)))
				return;
		stopovers.add(i, s);
	}
	
	private void addStopover(Stopover s) {
		addStopover(size(), s);
	}
	
	public void addStopover(int i, Coordinates c, long q) {
		addStopover(i, new Stopover(c, q));
	}
	
	public void addStopover(Coordinates c, long q) {
		addStopover(size(), c, q);
	}
	
	public Path append(Path p) {
		for(Stopover s : p.stopovers)
			addStopover(s);
		return this;
	}
	
	public Coordinates getCoordinates(int i) {
		return stopovers.get(i).coordinates;
	}
	
	public Coordinates getLastCoordinates() {
		return getCoordinates(size() - 1);
	}
	
	public Coordinates getFirstCoordinates() {
		return getCoordinates(0);
	}
	
	public long getQuantityToBringIn(int i) {
		return stopovers.get(i).quantityToBringIn;
	}
	
	public double length() {
		double length = 0.0;
		for(int i = 1; i < size(); i ++)
			length += getCoordinates(i - 1).distance(getCoordinates(i));
		return length;
	}
	
	public Path subPath(int firstToInclude, int firstToExclude) {
		return new Path(stopovers.subList(firstToInclude, firstToExclude));
	}
	
	public Path prefix(int firstToExclude) {
		return subPath(0, firstToExclude);
	}
	
	public Path suffix(int firstToInclude) {
		return subPath(firstToInclude, size());
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Stopover s : stopovers)
			sb.append(s.toString() + " -> ");
		return sb.substring(0, sb.length() - 4).toString();
	}
}

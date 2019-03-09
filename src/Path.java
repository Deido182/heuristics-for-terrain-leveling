import java.util.ArrayList;
import java.util.List;

class Stopover {
	
	/*
	 * We NEED the quantityToBringIn attribute for fixPath. 
	 * Otherwise we could assume that every time the truck moves 
	 * as much terrain as possible.
	 */
	
	Coordinates coordinates;
	double quantityToBringIn;
	
	public Stopover(Coordinates coordinates, double quantityToBringIn) {
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
	
	public Path(List<Stopover> stopovers) {
		this.stopovers = new ArrayList <> ();
		this.stopovers.addAll(stopovers);
	}
	
	public int length() {
		return stopovers.size();
	}
	
	public Stopover removeStopover(int i) {
		return stopovers.remove(i);
	}
	
	
	private void addStopover(int i, Stopover s) {
		if(i > 0)
			if(getCoordinates(i - 1).equals(s.coordinates))
				return;
		if(i < length())
			if(s.coordinates.equals(getCoordinates(i)))
				return;
		stopovers.add(i, s);
	}
	
	private void addStopover(Stopover s) {
		addStopover(length(), s);
	}
	
	public void addStopover(int i, Coordinates c, double q) {
		addStopover(i, new Stopover(c, q));
	}
	
	public void addStopover(Coordinates c, double q) {
		addStopover(length(), c, q);
	}
	
	public void rerouteOne(int after, Coordinates c) {
		Stopover removed = removeStopover(after + 1);
		addStopover(after + 1, c, removed.quantityToBringIn);
		addStopover(after + 2, removed);
	}
	
	public void rerouteTwo(int after, Coordinates c1, Coordinates c2) {
		rerouteOne(after, c1);
		rerouteOne(after + 1, c2);
	}
	
	public void append(Path p) {
		for(Stopover s : p.stopovers)
			addStopover(s);
	}
	
	public Coordinates getCoordinates(int i) {
		return stopovers.get(i).coordinates;
	}
	
	public Coordinates getLastCoordinates() {
		return getCoordinates(length() - 1);
	}
	
	public Coordinates getFirstCoordinates() {
		return getCoordinates(0);
	}
	
	public double getQuantityToBringIn(int i) {
		return stopovers.get(i).quantityToBringIn;
	}
	
	public double distance() {
		double distance = 0.0;
		for(int i = 1; i < length(); i ++)
			distance += getCoordinates(i - 1).distance(getCoordinates(i));
		return distance;
	}
	
	public Path prefix(int firstToExclude) {
		return new Path(stopovers.subList(0, firstToExclude));
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Stopover s : stopovers)
			sb.append(s.toString() + " -> ");
		return sb.substring(0, sb.length() - 4).toString();
	}
}

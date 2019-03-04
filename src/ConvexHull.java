import java.util.ArrayList;
import java.util.LinkedList;

public class ConvexHull {
	
	public static final double COLLINEAR_ERROR = 1E-6;
	
	public static int orientation(Coordinates c1, Coordinates c2, Coordinates c3) { 
	    double val = (c2.y - c1.y) * (c3.x - c2.x) - (c2.x - c1.x) * (c3.y - c2.y); 
	  
	    if (Math.abs(val) <= COLLINEAR_ERROR) 
	    	return 0;
	    return (val > COLLINEAR_ERROR)? 1 : 2; // clock or counterclock wise 
	} 
	
	public static Coordinates pullBottomLeft(ArrayList <Coordinates> coordinates) {
		int bottomLeftIndex = 0;
		for(int i = 1; i < coordinates.size(); i ++) 
			if(coordinates.get(i).y < coordinates.get(bottomLeftIndex).y || 
					(coordinates.get(i).sameY(coordinates.get(bottomLeftIndex)) && coordinates.get(i).x < coordinates.get(bottomLeftIndex).x))
				bottomLeftIndex = i;
		return coordinates.remove(bottomLeftIndex);
	}
	
	public static ArrayList <Coordinates> build(ArrayList <Coordinates> coordinates) {
		ArrayList <Coordinates> convexHull = new ArrayList <> ();
		
		if(coordinates.size() <= 2)
			return (ArrayList<Coordinates>) coordinates.clone();
		
		Coordinates bottomLeft = pullBottomLeft(coordinates);
		coordinates.sort((Coordinates c1, Coordinates c2) -> {
			int o = orientation(bottomLeft, c1, c2); 
			if (o == 0)
				return Double.compare(bottomLeft.distance(c1), bottomLeft.distance(c2));
			return (o == 2) ? -1 : 1;
		});
		
		ArrayList <Coordinates> newCoordinates = new ArrayList <> ();
		newCoordinates.add(bottomLeft);
		for(int i = 1; i < coordinates.size(); i ++) { 
			while (i < coordinates.size() - 1) {
				if(orientation(bottomLeft, coordinates.get(i), coordinates.get(i + 1)) != 0)
					break;
				i ++;
		    }
			newCoordinates.add(coordinates.get(i));
		}
		coordinates = newCoordinates;
		
		LinkedList <Coordinates> stack = new LinkedList <> (); 
		stack.push(coordinates.get(0)); 
		stack.push(coordinates.get(1)); 
		stack.push(coordinates.get(2)); 
		
		for(int i = 3; i < coordinates.size(); i++) {
			while(orientation(stack.get(1), stack.get(0), coordinates.get(i)) != 2) 
				stack.pop(); 
			stack.push(coordinates.get(i));
		} 
		
		convexHull.addAll(stack);
		return convexHull;
	}
}

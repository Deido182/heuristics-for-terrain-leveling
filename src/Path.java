
public class Path {
	private class Stopover {
		Coordinates coordinates;
		double quantityToBringIn;
		
		public Stopover(Coordinates coordinates, double quantityToBringIn) {
			this.coordinates = coordinates;
			this.quantityToBringIn = quantityToBringIn;
		}
	}
}

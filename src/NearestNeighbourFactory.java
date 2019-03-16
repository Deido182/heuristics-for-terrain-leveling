
public class NearestNeighbourFactory implements ChainsBuildersFactory {

	public ChainsBuilder getChainsBuilder(Field field, Truck truck) {
		return new NearestNeighbourChainsBuilder(field, truck);
	}
}


public class GRASP_Factory implements ChainsBuildersFactory {

	public ChainsBuilder getChainsBuilder(Field field, Truck truck) {
		return new GRASP_ChainsBuilder(field, truck, 0.1, 3);
	}
}

package resonatninduction.tilenetwork;

/**
 * Used on tiles that can contain more than one tile network. Currently WIP so don't use unless you
 * know what your doing. When using this use networks like items and store them in slots.
 * 
 * @author DarkGuardsman
 */
public interface INetworkContainer
{
	/**
	 * Gets a list of all networks slots and their connected networks. Used both to see the max
	 * limit of networks this tile may contain, and if there are networks currently in use
	 */
	public ITileNetwork[] getContainedNetworks();

	/** Sets the network in the given slot */
	public boolean setNetwork(int slot, ITileNetwork network);

	/** Gets the network in the slot */
	public ITileNetwork getNetwork(int slot);
}

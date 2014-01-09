package resonatninduction.tilenetwork;

import universalelectricity.api.energy.IEnergyContainer;

/**
 * Tiles that use NetworkSharedPower class should implements this. All methods in IElectricalStorage
 * should point to the network instead of the tile. This is why more energy methods are added to
 * this interface
 * 
 * @author DarkGuardsman
 */
public interface INetworkEnergyPart extends INetworkPart, IEnergyContainer
{
	/** Gets the energy stored in the part */
	public long getPartEnergy();

	/** Gets the max energy storage limit of the part */
	public long getPartMaxEnergy();

	/** Sets the energy stored in the part */
	public void setPartEnergy(long energy);
}

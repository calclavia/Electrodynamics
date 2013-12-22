package resonantinduction.wire.part;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.energy.IEnergyInterface;

/**
 * @author Calclavia
 * 
 */
public interface ITest
{
	/**
	 * Adds energy to an block. Returns the quantity of energy that was accepted. This should always
	 * return 0 if the block cannot be externally charged.
	 * 
	 * @param from Orientation the energy is sent in from.
	 * @param receive Maximum amount of energy (joules) to be sent into the block.
	 * @param doReceive If false, the charge will only be simulated.
	 * @return Amount of energy that was accepted by the block.
	 */
	public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive);

}

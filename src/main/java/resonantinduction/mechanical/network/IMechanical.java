package resonantinduction.mechanical.network;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.net.IConnectable;

public interface IMechanical extends IConnectable
{
	/**
	 * Adds energy to a block. Returns the quantity of energy that was accepted. This should always
	 * return 0 if the block cannot be externally charged.
	 * 
	 * @param from Orientation the energy is sent in from.
	 * @param receive Maximum amount of energy (joules) to be sent into the block.
	 * @param doReceive If false, the charge will only be simulated.
	 * @return Amount of energy that was accepted by the block.
	 */
	public long onReceiveEnergy(ForgeDirection from, long torque, float angularVelocity, boolean doReceive);

	public boolean isClockwise();

	public void setClockwise(boolean isClockwise);
}

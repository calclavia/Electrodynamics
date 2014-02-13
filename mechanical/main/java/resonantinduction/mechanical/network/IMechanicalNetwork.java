package resonantinduction.mechanical.network;

import universalelectricity.api.net.INetwork;

/**
 * Mechanical network in interface form for interaction or extension
 * 
 * @author DarkGuardsman
 */
public interface IMechanicalNetwork extends INetwork<IMechanicalNetwork, IMechanical>
{
	/**
	 * @return The current rotation value of the network. Used for syncing rotational values.
	 */
	public float getRotation(float velocity);
}

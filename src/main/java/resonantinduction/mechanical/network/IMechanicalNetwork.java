package resonantinduction.mechanical.network;

import universalelectricity.api.net.INetwork;

/**
 * Mechanical network in interface form for interaction or extension
 * 
 * @author DarkGuardsman
 */
public interface IMechanicalNetwork extends INetwork<IMechanicalNetwork, IMechanicalConnector, IMechanical>
{
	/**
	 * Gets the power of the network.
	 * 
	 * @return Power in Watts.
	 */
	public long getPower();

	public void setPower(long torque, float angularVelocity);

	/** Torque applied by the network at the given speed */
	public long getTorque();

	/**
	 * Gets the angular velocity of the network.
	 * 
	 * @return In radians per second.
	 */
	public float getAngularVelocity();

	public long getPrevTorque();

	public float getPrevAngularVelocity();

	/** Called to rebuild the network */
	@Override
	public void reconstruct();

	public long onReceiveEnergy(long torque, float angularVelocity);

	/**
	 * @return The current rotation value of the network.
	 */
	public float getRotation();
}

package resonantinduction.mechanical.network;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.net.IConnector;

public interface IMechanical extends IConnector<IMechanicalNetwork>
{
	/**
	 * The angular velocity.
	 * 
	 * @return Can be negative.
	 */
	public float getAngularVelocity();

	public void setAngularVelocity(float velocity);

	public long getTorque();

	public void setTorque(long torque);
	
	public float getRatio(ForgeDirection dir);

	public IMechanical getInstance(ForgeDirection dir);
}

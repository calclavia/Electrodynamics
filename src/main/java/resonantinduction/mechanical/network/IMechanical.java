package resonantinduction.mechanical.network;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.net.IConnector;
import universalelectricity.api.vector.Vector3;

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

	/**
	 * Can this components connect with the other?
	 * 
	 * @param from - The direction the connection is coming from relative to this block.
	 * @param source - The object trying to connect
	 * @return
	 */
	public boolean canConnect(ForgeDirection from, Object sourcen);

	public Vector3 getPosition();
}

package resonantinduction.api.mechanical;

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

	public float getRatio(ForgeDirection dir, Object source);

	public boolean inverseRotation(ForgeDirection dir, IMechanical with);

	@Override
	public IMechanical getInstance(ForgeDirection dir);

	public Vector3 getPosition();
}

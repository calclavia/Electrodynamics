package resonantinduction.api;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector3;

public interface IMechanicalNode extends IEnergyNode
{
	public double getTorque();

	public double getAngularVelocity();

	public void apply(double torque, double angularVelocity);

	public float getRatio(ForgeDirection dir, IMechanicalNode with);

	public boolean inverseRotation(ForgeDirection dir, IMechanicalNode with);

	public IMechanicalNode setLoad(double load);

	public Vector3 position();
}

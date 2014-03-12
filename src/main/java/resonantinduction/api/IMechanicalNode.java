package resonantinduction.api;

import resonantinduction.mechanical.energy.grid.MechanicalNode;
import net.minecraftforge.common.ForgeDirection;

public interface IMechanicalNode extends IEnergyNode
{
	public double getTorque();

	public double getAngularVelocity();

	public void apply(double torque, double angularVelocity);

	public float getRatio(ForgeDirection dir, MechanicalNode with);

	public boolean inverseRotation(ForgeDirection dir, MechanicalNode with);

	public IMechanicalNode setLoad(double load);
}

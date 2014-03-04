package resonantinduction.mechanical.energy.network;

import resonantinduction.core.grid.NodeGrid;
import universalelectricity.core.net.NetworkTickHandler;

/**
 * A mechanical network for translate speed and force using mechanical rotations.
 * 
 * Useful Formula:
 * 
 * Power is the work per unit time.
 * Power (W) = Torque (Strength of the rotation, Newton Meters) x Speed (Angular Velocity, RADIAN
 * PER SECOND).
 * *OR*
 * Power = Torque / Time
 * 
 * Torque = r (Radius) * F (Force) * sin0 (Direction/Angle of the force applied. 90 degrees if
 * optimal.)
 * 
 * @author Calclavia
 */
public class MechanicalNetwork extends NodeGrid<MechanicalNode>
{
	public MechanicalNetwork(MechanicalNode node)
	{
		super(MechanicalNode.class);
		add(node);
		NetworkTickHandler.addNetwork(this);
	}
}

package resonantinduction.core.interfaces;

import net.minecraftforge.common.util.ForgeDirection;
import universalelectricity.api.core.grid.INode;
import universalelectricity.core.transform.vector.IVectorWorld;

/**
 * Applied to any node that will act as a mechanical object
 *
 * @author Darkguardsman
 */
public interface IMechanicalNode extends INode, IVectorWorld
{
	/**
	 * Gets the radius of the gear in meters. Used to calculate torque and gear ratio for connections.
	 * Is not applied to direct face to face connections
	 *
	 * @param side - side of the machine
	 * @return radius in meters of the rotation peace
	 */
	public double getRadius(ForgeDirection side, IMechanicalNode with);

	/**
	 * The Rotational speed of the object
	 *
	 * @param side - side of the machine
	 * @return speed in meters per second
	 */
	public double getAngularSpeed(ForgeDirection side);

	/**
	 * Force applied from this side
	 *
	 * @param side - side of the machine
	 * @return force
	 */
	public double getForce(ForgeDirection side);

	/**
	 * Does the direction flip on this side for rotation
	 *
	 * @param side - side of the machine
	 * @return boolean, true = flipped, false = not
	 */
	public boolean inverseRotation(ForgeDirection side);

	/**
	 * Applies rotational force and velocity to this node increasing its current rotation value
	 *
	 * @param source          - should not be null
	 * @param torque          - force at an angle
	 * @param angularVelocity - speed of rotation
	 */
	public void apply(Object source, double torque, double angularVelocity);

}

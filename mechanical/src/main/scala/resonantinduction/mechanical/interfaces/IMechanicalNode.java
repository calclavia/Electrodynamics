package resonantinduction.mechanical.interfaces;

import net.minecraftforge.common.ForgeDirection;
import resonant.api.grid.INode;
import universalelectricity.api.vector.Vector3;

/** Applied to any node that will act as a mechanical object in the network
 * 
 * @author Darkguardsman */
public interface IMechanicalNode extends INode
{
    /** The Rotational force */
    public double getTorque();

    /** TODO remove */
    @Deprecated
    public double getEnergy();

    /** TODO remove */
    @Deprecated
    public double getPower();

    /** The Rotational velocity */
    public double getAngularVelocity();

    /** Applies rotational force and velocity to the mechanical object */
    public void apply(Object source, double torque, double angularVelocity);

    public float getRatio(ForgeDirection dir, IMechanicalNode with);

    @Deprecated
    public boolean inverseRotation(ForgeDirection dir, IMechanicalNode with);

    @Deprecated
    public IMechanicalNode setLoad(double load);

    @Deprecated
    public Vector3 position();

}

package resonantinduction.mechanical.gear.dev;

import net.minecraftforge.common.ForgeDirection;

/** Added to any device that generates a mechanical rotational force as part of the network
 * 
 * @author Darkguardsman */
public class NodeGenerator extends NodeGear
{
    /** Should the generator supply a force to the network. Checked each tick before calling any
     * force related methods.
     * 
     * @param side - side the force is outputting to
     * @return true if this node can */
    public boolean shouldSupplyForce(ForgeDirection side)
    {
        return false;
    }
}

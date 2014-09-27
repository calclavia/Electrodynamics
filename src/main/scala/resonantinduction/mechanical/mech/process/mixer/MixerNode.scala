package resonantinduction.mechanical.mech.process.mixer

import net.minecraftforge.common.util.ForgeDirection
import resonantinduction.mechanical.mech.MechanicalNode
import universalelectricity.api.core.grid.INodeProvider

/**
 * Node designed just for the Mixer to use
 * @param parent - instance of TileMixer that will host this node, should never be null
 */
class MixerNode(parent: INodeProvider) extends MechanicalNode(parent)
{
    //Constructor
    maxDeltaAngle = Math.toRadians(45)
    sharePower = false

    override def canConnect(direction: ForgeDirection): Boolean =
    {
        return direction == ForgeDirection.DOWN || direction == ForgeDirection.UP
    }

    override def inverseRotation(dir: ForgeDirection): Boolean =
    {
        return dir == ForgeDirection.DOWN
    }
}
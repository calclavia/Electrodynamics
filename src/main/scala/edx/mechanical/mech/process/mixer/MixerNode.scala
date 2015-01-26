package edx.mechanical.mech.process.mixer

import edx.mechanical.mech.grid.NodeMechanical
import net.minecraftforge.common.util.ForgeDirection
import resonantengine.api.graph.INodeProvider

/**
 * Node designed just for the Mixer to use
 * @param parent - instance of TileMixer that will host this node, should never be null
 */
class MixerNode(parent: INodeProvider) extends NodeMechanical(parent)
{
  override def canConnect(direction: ForgeDirection): Boolean =
  {
    return direction == ForgeDirection.DOWN || direction == ForgeDirection.UP
  }

  /*
  override def inverseRotation(dir: ForgeDirection): Boolean =
  {
      return dir == ForgeDirection.DOWN
  }*/
}
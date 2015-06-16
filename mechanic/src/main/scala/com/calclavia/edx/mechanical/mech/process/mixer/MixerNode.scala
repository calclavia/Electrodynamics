package com.calclavia.edx.mechanical.mech.process.mixer

import com.calclavia.edx.mechanical.mech.grid.MechanicalComponent
import net.minecraftforge.common.util.ForgeDirection
import resonantengine.api.graph.INodeProvider

/**
 * Node designed just for the Mixer to use
 * @param parent - instance of TileMixer that will host this node, should never be null
 */
class MixerNode(parent: INodeProvider) extends MechanicalComponent(parent)
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
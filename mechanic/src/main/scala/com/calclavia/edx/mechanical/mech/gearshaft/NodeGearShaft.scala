package com.calclavia.edx.mechanical.mech.gearshaft

import com.calclavia.edx.mechanical.mech.gear.{NodeGear, PartGear}
import com.calclavia.edx.mechanical.mech.grid.NodeMechanical
import edx.core.interfaces.TNodeMechanical
import edx.mechanical.mech.gear.PartGear
import net.minecraftforge.common.util.ForgeDirection
import resonantengine.api.graph.INodeProvider
import resonantengine.lib.transform.vector.Vector3
import resonantengine.lib.wrapper.ForgeDirectionWrapper._

class NodeGearShaft(parent: PartGearShaft) extends NodeMechanical(parent)
{
  override def inertia: Double =
  {
    return shaft.tier match
    {
      case 0 => 3
      case 1 => 5
      case 2 => 4
    }
  }

  def shaft: PartGearShaft = getParent.asInstanceOf[PartGearShaft]

  override def rebuild()
  {
    //Check only two possible sides for connections
    for (toDir <- Seq(shaft.placementSide, shaft.placementSide.getOpposite))
    {
      var found = false

      ///Check within this block for another gear plate that will move this shaft
      val otherNode = shaft.tile.asInstanceOf[INodeProvider].getNode(classOf[NodeMechanical], toDir)

      if (otherNode != null && otherNode != this && canConnect(otherNode, toDir) && otherNode.canConnect(this, toDir.getOpposite))
      {
        connect(otherNode, toDir)
        found = true
      }

      if (!found)
      {
        ///Check for other gear shafts outside this tile
        val checkTile = new Vector3(shaft.tile).add(toDir).getTileEntity(world)

        if (checkTile.isInstanceOf[INodeProvider])
        {
          val instance = checkTile.asInstanceOf[INodeProvider].getNode(classOf[NodeMechanical], toDir.getOpposite)

          if (instance != null && instance != this && instance.getParent.isInstanceOf[PartGearShaft] && canConnect(instance, toDir) && instance.canConnect(this, toDir.getOpposite))
          {
            connect(instance, toDir)
          }
        }
      }
    }
  }

  override def canConnect[B](other: B, from: ForgeDirection): Boolean =
  {
    if (other.isInstanceOf[NodeMechanical])
    {
      if (other.asInstanceOf[NodeMechanical].getParent.isInstanceOf[PartGear])
      {
        val gear = other.asInstanceOf[NodeMechanical].getParent.asInstanceOf[PartGear]
        if (!(Math.abs(gear.placementSide.offsetX) == Math.abs(shaft.placementSide.offsetX) && Math.abs(gear.placementSide.offsetY) == Math.abs(shaft.placementSide.offsetY) && Math.abs(gear.placementSide.offsetZ) == Math.abs(shaft.placementSide.offsetZ)))
        {
          return false
        }
      }
    }

    return from == shaft.placementSide || from == shaft.placementSide.getOpposite
  }

  override def inverseRotation(other: TNodeMechanical): Boolean = other.isInstanceOf[NodeGear] && other.asInstanceOf[NodeGear].parent.asInstanceOf[PartGear].placementSide.offset < Vector3.zero
}
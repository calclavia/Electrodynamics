package resonantinduction.mechanical.mech.gearshaft

import net.minecraftforge.common.util.ForgeDirection
import resonant.api.grid.INodeProvider
import resonant.lib.transform.vector.Vector3
import resonantinduction.mechanical.mech.gear.PartGear
import resonantinduction.mechanical.mech.grid.MechanicalNode

class GearShaftNode(parent: PartGearShaft) extends MechanicalNode(parent)
{
  override def getTorqueLoad: Double =
  {
    return shaft.tier match
    {
      case 0 => 0.03
      case 1 => 0.02
      case 2 => 0.01
    }
  }

  override def getAngularVelocityLoad: Double =
  {
    return shaft.tier match
    {
      case 0 => 0.03
      case 1 => 0.02
      case 2 => 0.01
    }
  }

  override def rebuild()
  {
    //Check only two possible sides for connections
    for (toDir <- Seq(shaft.placementSide, shaft.placementSide.getOpposite))
    {
      var found = false

      ///Check within this block for another gear plate that will move this shaft
      val otherNode = shaft.tile.asInstanceOf[INodeProvider].getNode(classOf[MechanicalNode], toDir)

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
          val instance = (checkTile.asInstanceOf[INodeProvider]).getNode(classOf[MechanicalNode], toDir.getOpposite)

          if (instance != null && instance != this && instance.getParent.isInstanceOf[PartGearShaft] && instance.canConnect(this, toDir.getOpposite))
          {
            connect(instance, toDir)
          }
        }
      }
    }
  }

  override def canConnect[B](other: B, from: ForgeDirection): Boolean =
  {
    if (other.isInstanceOf[MechanicalNode])
    {
      if ((other.asInstanceOf[MechanicalNode]).getParent.isInstanceOf[PartGear])
      {
        val gear: PartGear = (other.asInstanceOf[MechanicalNode]).getParent.asInstanceOf[PartGear]
        if (!(Math.abs(gear.placementSide.offsetX) == Math.abs(shaft.placementSide.offsetX) && Math.abs(gear.placementSide.offsetY) == Math.abs(shaft.placementSide.offsetY) && Math.abs(gear.placementSide.offsetZ) == Math.abs(shaft.placementSide.offsetZ)))
        {
          return false
        }
      }
    }
    return from == shaft.placementSide || from == shaft.placementSide.getOpposite
  }

  override def inverseRotation(dir: ForgeDirection): Boolean = false

  def shaft: PartGearShaft = getParent.asInstanceOf[PartGearShaft]
}
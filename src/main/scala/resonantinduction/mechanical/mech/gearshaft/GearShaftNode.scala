package resonantinduction.mechanical.mech.gearshaft

import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection
import resonantinduction.mechanical.mech.gear.PartGear
import resonant.api.grid.INodeProvider
import resonant.lib.transform.vector.Vector3
import resonantinduction.mechanical.mech.grid.MechanicalNode

class GearShaftNode(parent: INodeProvider) extends MechanicalNode(parent)
{
  override def getTorqueLoad: Double =
  {
    shaft.tier match
    {
      case 1 =>
        return 0.02
      case 2 =>
        return 0.01
      case _ =>
        return 0.03
    }
  }

  override def getAngularVelocityLoad: Double =
  {
    return 0
  }

  override def reconstruct()
  {
    connections.clear

    for (ch <- List(shaft.placementSide, shaft.placementSide.getOpposite))
    {
      val checkDir: ForgeDirection = ch
      if (checkDir == shaft.placementSide || checkDir == shaft.placementSide.getOpposite)
      {
        if (shaft.tile.isInstanceOf[INodeProvider])
        {
          val instance: MechanicalNode = (shaft.tile.asInstanceOf[INodeProvider]).getNode(classOf[MechanicalNode], checkDir).asInstanceOf[MechanicalNode]

          if (instance != null && instance != this && instance.canConnect(this, checkDir.getOpposite))
          {
            connect(instance, checkDir)
          }
        }
        else
        {
          val checkTile: TileEntity = new Vector3(shaft.tile).add(checkDir).getTileEntity(world)
          if (checkTile.isInstanceOf[INodeProvider])
          {
            val instance: MechanicalNode = (checkTile.asInstanceOf[INodeProvider]).getNode(classOf[MechanicalNode], checkDir.getOpposite).asInstanceOf[MechanicalNode]
            if (instance != null && instance != this && instance.getParent.isInstanceOf[PartGearShaft] && instance.canConnect(this, checkDir.getOpposite))
            {
              connect(instance, checkDir)
            }
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

  override def inverseRotation(dir: ForgeDirection): Boolean =
  {
    if (shaft.placementSide.offsetY != 0 || shaft.placementSide.offsetZ != 0)
    {
      return dir eq shaft.placementSide.getOpposite
    }
    return dir eq shaft.placementSide
  }

  def shaft: PartGearShaft =
  {
    return this.getParent.asInstanceOf[PartGearShaft]
  }
}
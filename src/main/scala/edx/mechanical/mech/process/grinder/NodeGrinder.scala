package edx.mechanical.mech.process.grinder

import edx.core.interfaces.TNodeMechanical
import edx.mechanical.mech.gear.NodeGear
import edx.mechanical.mech.grid.NodeMechanical
import net.minecraftforge.common.util.ForgeDirection

/**
 * @author Calclavia
 */
class NodeGrinder(parent: TileGrindingWheel) extends NodeMechanical(parent: TileGrindingWheel)
{
  override def getLoad = 1000d * Math.abs(angularVelocity)

  override def canConnect[B <: NodeMechanical](other: B, from: ForgeDirection): Boolean = parent.getDirection == from || parent.getDirection.getOpposite == from

  override def inverseRotation(other: TNodeMechanical): Boolean = if (other.isInstanceOf[NodeGear]) (toVector3 - other.asInstanceOf[NodeMechanical].toVector3).toArray.sum < 0 else false

  override def inverseNext(other: TNodeMechanical): Boolean = if (other.isInstanceOf[NodeGear]) (toVector3 - other.asInstanceOf[NodeMechanical].toVector3).toArray.sum < 0 else false
}
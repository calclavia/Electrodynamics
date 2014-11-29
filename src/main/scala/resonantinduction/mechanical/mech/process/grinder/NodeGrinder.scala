package resonantinduction.mechanical.mech.process.grinder

import net.minecraftforge.common.util.ForgeDirection
import resonantinduction.core.interfaces.TNodeMechanical
import resonantinduction.mechanical.mech.gear.NodeGear
import resonantinduction.mechanical.mech.grid.NodeMechanical

/**
 * @author Calclavia
 */
class NodeGrinder(parent: TileGrindingWheel) extends NodeMechanical(parent: TileGrindingWheel)
{
  override def getLoad = 1000d * angularVelocity

  override def canConnect[B <: NodeMechanical](other: B, from: ForgeDirection): Boolean = parent.getDirection == from || parent.getDirection.getOpposite == from

  override def inverseRotation(other: TNodeMechanical): Boolean = if (other.isInstanceOf[NodeGear])  (toVector3 - other.asInstanceOf[NodeMechanical].toVector3).toArray.sum < 0 else false

  override def inverseNext(other: TNodeMechanical): Boolean = if (other.isInstanceOf[NodeGear]) (toVector3 - other.asInstanceOf[NodeMechanical].toVector3).toArray.sum < 0 else super.inverseNext(other)
}
package com.calclavia.edx.mechanical.mech.process.grinder

import com.calclavia.edx.mechanical.mech.gear.NodeGear
import com.calclavia.edx.mechanical.mech.grid.NodeMechanical
import edx.core.interfaces.TNodeMechanical
import net.minecraftforge.common.util.ForgeDirection

/**
 * @author Calclavia
 */
class NodeGrinder(parent: TileGrindingWheel) extends NodeMechanical(parent: TileGrindingWheel)
{
  override def inertia = 1000d * Math.abs(angularVelocity)

  override def canConnect[B <: NodeMechanical](other: B, from: ForgeDirection): Boolean = parent.getDirection == from || parent.getDirection.getOpposite == from

  override def inverseRotation(other: TNodeMechanical): Boolean = if (other.isInstanceOf[NodeGear]) (position - other.asInstanceOf[NodeMechanical].position).toArray.sum < 0 else false

  override def inverseNext(other: TNodeMechanical): Boolean = if (other.isInstanceOf[NodeGear]) (position - other.asInstanceOf[NodeMechanical].position).toArray.sum < 0 else false
}
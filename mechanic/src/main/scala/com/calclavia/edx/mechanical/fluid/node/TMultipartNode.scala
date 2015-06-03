package com.calclavia.edx.core.prefab.node

import codechicken.multipart.TMultiPart
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import resonantengine.lib.grid.core.NodeConnector

/**
 * A trait that allows nodes to works with Forge Multipart. This trait MUST be mixed in.
 * @author Calclavia
 */
trait TMultipartNode[A <: AnyRef] extends NodeConnector[A]
{
  override def world: World =
  {
    return parent match
    {
      case p: TMultiPart => p.world
      case p: TileEntity => p.getWorldObj
    }
  }

  override def x: Double =
  {
    return parent match
    {
      case p: TMultiPart => p.x
      case p: TileEntity => p.xCoord
    }
  }

  override def y: Double =
  {
    return parent match
    {
      case p: TMultiPart => p.y
      case p: TileEntity => p.yCoord
    }
  }

  override def z: Double =
  {
    return parent match
    {
      case p: TMultiPart => p.z
      case p: TileEntity => p.zCoord
    }
  }
}

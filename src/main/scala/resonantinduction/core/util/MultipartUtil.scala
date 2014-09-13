package resonantinduction.core.util

import codechicken.lib.vec.BlockCoord
import codechicken.multipart.{TMultiPart, TileMultipart}
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.common.util.ForgeDirection
import universalelectricity.core.transform.vector.{Vector3, VectorWorld}

/**
 * Multipart Utilities
 *
 * @author Calclavia
 *
 */
object MultipartUtil
{
  def getMultipartTile(access: IBlockAccess, pos: BlockCoord): TileMultipart =
  {
    val te: TileEntity = access.getTileEntity(pos.x, pos.y, pos.z)
    return if (te.isInstanceOf[TileMultipart]) te.asInstanceOf[TileMultipart] else null
  }

  def getMultipart(world: World, vector: Vector3, partMap: Int): TMultiPart =
  {
    return getMultipart(new VectorWorld(world, vector), partMap)
  }

  def getMultipart(vector: VectorWorld, partMap: Int): TMultiPart =
  {
    return getMultipart(vector.world, vector.xi, vector.yi, vector.zi, partMap)
  }

  def getMultipart(world: World, x: Int, y: Int, z: Int, partMap: Int): TMultiPart =
  {
    val tile: TileEntity = world.getTileEntity(x, y, z)
    if (tile.isInstanceOf[TileMultipart])
    {
      return (tile.asInstanceOf[TileMultipart]).partMap(partMap)
    }
    return null
  }

  def canPlaceWireOnSide(w: World, x: Int, y: Int, z: Int, side: ForgeDirection, _default: Boolean): Boolean =
  {
    if (!w.blockExists(x, y, z)) return _default
    val b: Block = w.getBlock(x, y, z)
    if (b == null) return false
    if (b == Blocks.glowstone || b == Blocks.piston || b == Blocks.sticky_piston || b == Blocks.piston_extension) return true
    return b.isSideSolid(w, x, y, z, side)
  }
}
package resonantinduction.core.resource

import java.util

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{AxisAlignedBB, MovingObjectPosition}
import net.minecraft.world.{IBlockAccess, World}
import resonantinduction.core.CoreContent
import universalelectricity.core.transform.region.Cuboid

/**
 * @author Calclavia
 */
class TileDust extends TileMaterial(Material.sand)
{
  private[resource] var nextDropMaterialID: Int = 0

  bounds = new Cuboid(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F)
  setBlockBoundsForDepth(0)
  blockHardness = 0.5f
  textureName = "material_sand"
  stepSound = Block.soundTypeGravel
  normalRender = false
  isOpaqueCube = false

  def canFallBelow(world: World, x: Int, y: Int, z: Int): Boolean =
  {
    val block = world.getBlock(x, y, z)

    if (world.isAirBlock(x, y, z))
    {
      return true
    }
    else if (block == Blocks.fire)
    {
      return true
    }
    else
    {
      val material = block.getMaterial
      return if (material == Material.water) true else material == Material.lava
    }
  }

  @SideOnly(Side.CLIENT)
  override def colorMultiplier: Int = getColor

  override def onPlaced(entityLiving: EntityLivingBase, itemStack: ItemStack)
  {
    materialName = ItemResourceDust.getMaterialFromStack(itemStack)
  }

  override def onPostPlaced(metadata: Int)
  {
    tryToFall()
  }

  /**
   * Block events
   */
  override def onAdded()
  {
    tryToFall()
  }

  override def onNeighborChanged(block: Block)
  {
    tryToFall()
  }

  private def tryToFall()
  {
    var y = this.y

    val tile: TileEntity = world.getTileEntity(x, y, z)
    if (tile.isInstanceOf[TileMaterial])
    {
      val materialName: String = (tile.asInstanceOf[TileMaterial]).materialName
      if (materialName != null)
      {
        val metadata: Int = world.getBlockMetadata(x, y, z)
        if (canFallBelow(world, x, y - 1, z) && y >= 0)
        {
          world.setBlockToAir(x, y, z)
          while (canFallBelow(world, x, y - 1, z) && y > 0)
          {
            y -= 1
          }
          if (y > 0)
          {
            world.setBlock(x, y, z, block, metadata, 3)

            val newTile: TileEntity = world.getTileEntity(x, y, z)
            if (newTile.isInstanceOf[TileMaterial])
            {
              (newTile.asInstanceOf[TileMaterial]).materialName = materialName
            }
          }
        }
      }
    }
  }

  /**
   * Returns a bounding box from the pool of bounding boxes (this means this box can change after
   * the pool has been
   * cleared to be reused)
   */
  def getCollisionBoundingBoxFromPool(par1World: World, par2: Int, par3: Int, par4: Int): AxisAlignedBB =
  {
    val l: Int = par1World.getBlockMetadata(par2, par3, par4) & 7
    val f: Float = 0.125F
    return AxisAlignedBB.getBoundingBox(par2 + bounds.min.x, par3 + bounds.min.y, par4 + bounds.min.z, par2 + bounds.max.x, par3 + l * f, par4 + bounds.max.z)
  }

  /**
   * Sets the block's bounds for rendering it as an item
   */
  override def setBlockBoundsForItemRender
  {
    this.setBlockBoundsForDepth(0)
  }

  /**
   * Called upon bounds raytrace. World data given.
   */
  override def setBlockBoundsBasedOnState()
  {
    setBlockBoundsForDepth(metadata)
  }

  /**
   * calls setBlockBounds based on the depth of the snow. Int is any values 0x0-0x7, usually this
   * blocks metadata.
   */
  protected def setBlockBoundsForDepth(meta: Int)
  {
    val j: Int = meta & 7
    val f: Float = 2 * (1 + j) / 16.0F
    bounds = new Cuboid(0.0F, 0.0F, 0.0F, 1.0F, f, 1.0F)
  }

  /**
   * Drops
   */
  override def getDrops(metadata: Int, fortune: Int): util.ArrayList[ItemStack] =
  {
    val list = new util.ArrayList[ItemStack]

    if (block == CoreContent.blockRefinedDust)
      list.add(ResourceGenerator.getRefinedDust(materialName, quantityDropped(metadata, fortune)))
    else
      list.add(ResourceGenerator.getDust(materialName, quantityDropped(metadata, fortune)))

    return list
  }

  override def getPickBlock(target: MovingObjectPosition): ItemStack =
  {
    if (block == CoreContent.blockRefinedDust)
      return ResourceGenerator.getRefinedDust(materialName)
    else
      return ResourceGenerator.getDust(materialName)
  }

  @SideOnly(Side.CLIENT)
  override def shouldSideBeRendered(access: IBlockAccess, x: Int, y: Int, z: Int, side: Int): Boolean =
  {
    if (side == 1) true else super.shouldSideBeRendered(access, x, y, z, side)
  }

  override def quantityDropped(meta: Int, fortune: Int): Int = (meta & 7) + 1
}

package resonantinduction.core.resource

import java.util

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{AxisAlignedBB, MovingObjectPosition}
import net.minecraft.world.{IBlockAccess, World}
import resonantinduction.core.{CoreContent, Reference}

/**
 * @author Calclavia
 */
class TileDust extends TileMaterial(Material.sand)
{
  private[resource] var nextDropMaterialID: Int = 0

  setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F)
  setBlockBoundsForDepth(0)
  setHardness(0.5f)
  setTextureName(Reference.PREFIX + "material_sand")
  setStepSound(soundGravelFootstep)
  normalRender = false
  isOpaqueCube = false

  def canFallBelow(par0World: World, par1: Int, par2: Int, par3: Int): Boolean =
  {
    val l: Int = par0World.getBlockId(par1, par2, par3)
    if (par0World.isAirBlock(par1, par2, par3))
    {
      return true
    }
    else if (l == Block.fire.blockID)
    {
      return true
    }
    else
    {
      val material: Material = Block.blocksList(l).blockMaterial
      return if (material eq Material.water) true else material eq Material.lava
    }
  }

  @SideOnly(Side.CLIENT)
  override def colorMultiplier: Int = super.colorMultiplier
  {
    return getColor
  }

  override def onPlaced(entityLiving: EntityLivingBase, itemStack: ItemStack)
  {
    name = ItemResourceDust.getMaterialFromStack(itemStack)
  }

  override def onPostBlockPlaced(world: World, x: Int, y: Int, z: Int, metadata: Int)
  {
    tryToFall(world, x, y, z)
  }

  /**
   * Block events
   */
  override def onAdded()
  {
    tryToFall(world, x, y, z)
  }

  override def onNeighborChanged(block: Block)
  {
    tryToFall(world, x, y, z)
  }

  private def tryToFall(world: World, x: Int, y: Int, z: Int)
  {
    val tile: TileEntity = world.getBlockTileEntity(x, y, z)
    if (tile.isInstanceOf[TileMaterial])
    {
      val materialName: String = (tile.asInstanceOf[TileMaterial]).name
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
            world.setBlock(x, y, z, this.blockID, metadata, 3)
            val newTile: TileEntity = world.getBlockTileEntity(x, y, z)
            if (newTile.isInstanceOf[TileMaterial])
            {
              (newTile.asInstanceOf[TileMaterial]).name = materialName
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
    return AxisAlignedBB.getAABBPool.getAABB(par2 + this.minX, par3 + this.minY, par4 + this.minZ, par2 + this.maxX, par3 + l * f, par4 + this.maxZ)
  }

  /**
   * Sets the block's bounds for rendering it as an item
   */
  override def setBlockBoundsForItemRender
  {
    this.setBlockBoundsForDepth(0)
  }

  /**
   * Updates the blocks bounds based on its current state. Args: world, x, y, z
   */
  def setBlockBoundsBasedOnState(par1IBlockAccess: IBlockAccess, par2: Int, par3: Int, par4: Int)
  {
    this.setBlockBoundsForDepth(par1IBlockAccess.getBlockMetadata(par2, par3, par4))
  }

  /**
   * calls setBlockBounds based on the depth of the snow. Int is any values 0x0-0x7, usually this
   * blocks metadata.
   */
  protected def setBlockBoundsForDepth(par1: Int)
  {
    val j: Int = par1 & 7
    val f: Float = 2 * (1 + j) / 16.0F
    this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, f, 1.0F)
  }

  /**
   * Drops
   */
  override def getDrops(metadata: Int, fortune: Int): util.ArrayList[ItemStack] =
  {
    if (this == CoreContent.blockRefinedDust)
      return List(new ItemStack(CoreContent.refinedDust))

    return List(new ItemStack(CoreContent.dust))
  }

  override def getPickBlock(target: MovingObjectPosition): ItemStack = if (this == CoreContent.blockRefinedDust) new ItemStack(CoreContent.refinedDust) else new ItemStack(CoreContent.dust)

  override def onRemove(block: Block, par6: Int)
  {
    val tileEntity: TileEntity = world.getBlockTileEntity(x, y, z)
    if (tileEntity.isInstanceOf[TileMaterial])
    {
      nextDropMaterialID = ResourceGenerator.getID((tileEntity.asInstanceOf[TileMaterial]).name)
    }
  }

  def getDamageValue(world: World, x: Int, y: Int, z: Int): Int =
  {
    ResourceGenerator.getID((tileEntity.asInstanceOf[TileMaterial]).name)
  }

  override def metadataDropped(meta: Int, fortune: Int): Int = nextDropMaterialID

  @SideOnly(Side.CLIENT)
  override def shouldSideBeRendered(access: IBlockAccess, x: Int, y: Int, z: Int, side: Int): Boolean =
  {
    if (par5 == 1) true else super.shouldSideBeRendered(access, x, y, z, side)
  }

  override def quantityDropped(meta: Int, fortune: Int): Int = (meta & 7) + 1
}

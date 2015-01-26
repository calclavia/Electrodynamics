package edx.core.resource.content

import edx.basic.BasicContent
import edx.core.Reference
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.world.World
import resonantengine.lib.factory.resources.item.TItemResource
import resonantengine.lib.transform.vector.Vector3

/**
 * @author Calclavia
 */
class ItemRefinedDust extends Item with TItemResource
{
  setTextureName(Reference.prefix + "oreRefinedDust")
  setCreativeTab(CreativeTabs.tabMaterials)

  override def onItemUse(itemStack: ItemStack, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float): Boolean =
  {
    val block = world.getBlock(x, y, z)

    if (block == getBlock)
    {
      val i1: Int = world.getBlockMetadata(x, y, z)
      val j1: Int = i1 & 7
      if (j1 <= 6 && world.checkNoEntityCollision(this.getBlock.getCollisionBoundingBoxFromPool(world, x, y, z)) && world.setBlockMetadataWithNotify(x, y, z, j1 + 1 | i1 & -8, 2))
      {
        world.playSoundEffect((x.toFloat + 0.5F).toDouble, (y.toFloat + 0.5F).toDouble, (z.toFloat + 0.5F).toDouble, this.getBlock.stepSound.func_150496_b, (this.getBlock.stepSound.getVolume + 1.0F) / 2.0F, this.getBlock.stepSound.getPitch * 0.8F)
        itemStack.stackSize -= 1
        return true
      }
    }

    var actualSide = side
    val placeVec = new Vector3(x, y, z)

    if (block == Blocks.snow_layer && (world.getBlockMetadata(x, y, z) & 7) < 1)
    {
      actualSide = 1
    }
    else if (block != Blocks.vine && block != Blocks.tallgrass && block != Blocks.deadbush && !block.isReplaceable(world, x, y, z))
    {
      if (actualSide == 0)
      {
        placeVec.y -= 1
      }
      if (actualSide == 1)
      {
        placeVec.y += 1
      }
      if (actualSide == 2)
      {
        placeVec.z -= 1
      }
      if (actualSide == 3)
      {
        placeVec.z += 1
      }
      if (actualSide == 4)
      {
        placeVec.x -= 1
      }
      if (actualSide == 5)
      {
        placeVec.x += 1
      }
    }

    if (itemStack.stackSize == 0)
    {
      return false
    }
    else if (!player.canPlayerEdit(placeVec.xi, placeVec.yi, placeVec.zi, actualSide, itemStack))
    {
      return false
    }
    else if (y == 255 && getBlock.getMaterial.isSolid)
    {
      return false
    }
    else if (world.canPlaceEntityOnSide(this.getBlock, placeVec.xi, placeVec.yi, placeVec.zi, false, actualSide, player, itemStack))
    {
      val meta = this.getMetadata(itemStack.getItemDamage)
      val j1: Int = this.getBlock.onBlockPlaced(world, placeVec.xi, placeVec.yi, placeVec.zi, actualSide, placeVec.xi, placeVec.yi, placeVec.zi, meta)

      if (placeBlockAt(itemStack, player, world, placeVec.xi, placeVec.yi, placeVec.zi, actualSide, placeVec.xi, placeVec.yi, placeVec.zi, j1))
      {
        world.playSoundEffect((x.toFloat + 0.5F).toDouble, (y.toFloat + 0.5F).toDouble, (z.toFloat + 0.5F).toDouble, this.getBlock.stepSound.func_150496_b, (this.getBlock.stepSound.getVolume + 1.0F) / 2.0F, this.getBlock.stepSound.getPitch * 0.8F)
        itemStack.stackSize -= 1
      }
      return true
    }
    else
    {
      return false
    }
  }

  def placeBlockAt(stack: ItemStack, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float, metadata: Int): Boolean =
  {
    if (!world.setBlock(x, y, z, getBlock, metadata, 3))
    {
      return false
    }
    if (world.getBlock(x, y, z) == getBlock)
    {
      getBlock.onBlockPlacedBy(world, x, y, z, player, stack)
      getBlock.onPostBlockPlaced(world, x, y, z, metadata)
    }
    return true
  }

  def getBlock = BasicContent.blockDust
}

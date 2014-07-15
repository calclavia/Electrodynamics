package resonantinduction.core.resource.fluid

import java.util.List

import cpw.mods.fml.common.eventhandler.Event
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.MovingObjectPosition
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.FillBucketEvent
import net.minecraftforge.fluids.{BlockFluidFinite, FluidRegistry}
import resonant.lib.utility.LanguageUtility
import resonantinduction.core.resource.ResourceGenerator

/** Modified version of the MC bucket to meet the needs of a dynamic fluid registry system
  *
  * @author Calclavia */
class ItemResourceBucket(isMolten: Boolean) extends Item
{
  setMaxStackSize(1)
  setHasSubtypes(true)
  setMaxDamage(0)

  def getMaterialFromStack(itemStack: ItemStack): String =
  {
    return ResourceGenerator.getName(itemStack.getItemDamage)
  }

  override def getItemStackDisplayName(stack: ItemStack): String =
  {
    val material: String = getMaterialFromStack(stack)
    if (material != null)
    {
      val fluidID: String = if (isMolten) ResourceGenerator.materialNameToMolten(material) else ResourceGenerator.materialNameToMixture(material)
      if (fluidID != null && FluidRegistry.getFluid(fluidID) != null)
      {
        val fluidName: String = FluidRegistry.getFluid(fluidID).getLocalizedName
        return (LanguageUtility.getLocal(this.getUnlocalizedName + ".name")).replace("%v", fluidName).replace("  ", " ")
      }
      return material
    }
    return null
  }

  /** Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack,
    * world, entityPlayer */
  override def onItemRightClick(itemStack: ItemStack, world: World, entityPlayer: EntityPlayer): ItemStack =
  {
    val materialName: String = ResourceGenerator.getName(itemStack.getItemDamage)
    if (materialName != null)
    {
      val moltenBlock: BlockFluidFinite = ResourceGenerator.getMolten(materialName)
      val mixBlock: BlockFluidFinite = ResourceGenerator.getMixture(materialName)
      val fluid: Block = if (isMolten) if (moltenBlock != null) moltenBlock else null else if (mixBlock != null) mixBlock else null
      if (fluid != null)
      {
        val movingobjectposition: MovingObjectPosition = this.getMovingObjectPositionFromPlayer(world, entityPlayer, false)
        if (movingobjectposition == null)
        {
          return itemStack
        }
        else
        {
          val event: FillBucketEvent = new FillBucketEvent(entityPlayer, itemStack, world, movingobjectposition)
          if (MinecraftForge.EVENT_BUS.post(event))
          {
            return itemStack
          }
          if (event.getResult eq Event.Result.ALLOW)
          {
            if (entityPlayer.capabilities.isCreativeMode)
            {
              return itemStack
            }
            if (({itemStack.stackSize -= 1; itemStack.stackSize }) <= 0)
            {
              return event.result
            }
            if (!entityPlayer.inventory.addItemStackToInventory(event.result))
            {
              entityPlayer.dropPlayerItemWithRandomChoice(event.result, false)
            }
            return itemStack
          }
          if (movingobjectposition.typeOfHit eq MovingObjectPosition.MovingObjectType.BLOCK)
          {
            var i: Int = movingobjectposition.blockX
            var j: Int = movingobjectposition.blockY
            var k: Int = movingobjectposition.blockZ
            if (!world.canMineBlock(entityPlayer, i, j, k))
            {
              return itemStack
            }
            if (fluid == null)
            {
              if (!entityPlayer.canPlayerEdit(i, j, k, movingobjectposition.sideHit, itemStack))
              {
                return itemStack
              }
              if (world.getBlock(i, j, k).getMaterial eq Material.water && world.getBlockMetadata(i, j, k) == 0)
              {
                world.setBlockToAir(i, j, k)
                if (entityPlayer.capabilities.isCreativeMode)
                {
                  return itemStack
                }
                if (({itemStack.stackSize -= 1; itemStack.stackSize }) <= 0)
                {
                  return new ItemStack(Items.water_bucket)
                }
                if (!entityPlayer.inventory.addItemStackToInventory(new ItemStack(Items.water_bucket)))
                {
                  entityPlayer.dropPlayerItemWithRandomChoice(new ItemStack(Items.water_bucket, 1, 0), false)
                }
                return itemStack
              }
              if (world.getBlock(i, j, k).getMaterial eq Material.lava && world.getBlockMetadata(i, j, k) == 0)
              {
                world.setBlockToAir(i, j, k)
                if (entityPlayer.capabilities.isCreativeMode)
                {
                  return itemStack
                }
                if (({itemStack.stackSize -= 1; itemStack.stackSize }) <= 0)
                {
                  return new ItemStack(Items.lava_bucket)
                }
                if (!entityPlayer.inventory.addItemStackToInventory(new ItemStack(Items.lava_bucket)))
                {
                  entityPlayer.dropPlayerItemWithRandomChoice(new ItemStack(Items.lava_bucket, 1, 0), false)
                }
                return itemStack
              }
            }
            else
            {
              if (fluid != null)
              {
                return new ItemStack(Items.bucket)
              }
              if (movingobjectposition.sideHit == 0)
              {
                j -= 1
              }
              if (movingobjectposition.sideHit == 1)
              {
                j += 1
              }
              if (movingobjectposition.sideHit == 2)
              {
                k -= 1
              }
              if (movingobjectposition.sideHit == 3)
              {
                k += 1
              }
              if (movingobjectposition.sideHit == 4)
              {
                i -= 1
              }
              if (movingobjectposition.sideHit == 5)
              {
                i += 1
              }
              if (!entityPlayer.canPlayerEdit(i, j, k, movingobjectposition.sideHit, itemStack))
              {
                return itemStack
              }
              if (this.tryPlaceContainedLiquid(world, i, j, k, fluid) && !entityPlayer.capabilities.isCreativeMode)
              {
                return new ItemStack(Items.bucket)
              }
            }
          }
        }
      }
    }
    return itemStack
  }

  /** Attempts to place the liquid contained inside the bucket. */
  def tryPlaceContainedLiquid(world: World, x: Int, y: Int, z: Int, fluidID: Block): Boolean =
  {
    if (fluidID == null)
    {
      return false
    }
    else
    {
      val material: Material = world.getBlock(x, y, z).getMaterial
      val flag: Boolean = !material.isSolid
      if (!world.isAirBlock(x, y, z) && !flag)
      {
        return false
      }
      else
      {
        if (!world.isRemote && flag && !material.isLiquid)
        {
          world.setBlockToAir(x, y, z)
        }
        world.setBlock(x, y, z, fluidID, 8, 3)
        return true
      }
    }
  }

  def getStackFromMaterial(name: String): ItemStack =
  {
    val itemStack: ItemStack = new ItemStack(this)
    itemStack.setItemDamage(ResourceGenerator.getID(name))
    return itemStack
  }

  override def getSubItems(item: Item, tabs: CreativeTabs, list: List[_])
  {
    import scala.collection.JavaConversions._
    for (materialName <- ResourceGenerator.materials.keySet)
    {
      list.add(getStackFromMaterial(materialName))
    }
  }

  @SideOnly(Side.CLIENT)
  override def getColorFromItemStack(itemStack: ItemStack, par2: Int): Int =
  {
    val name = ResourceGenerator.getMaterialFromStack(itemStack)
    return ResourceGenerator.getColor(name)
  }
}
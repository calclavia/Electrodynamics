package resonantinduction.archaic.process

import cpw.mods.fml.relauncher.{Side, SideOnly}
import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.recipe.{MachineRecipes, RecipeResource}
import resonant.lib.factory.resources.RecipeType
import resonant.lib.network.discriminator.PacketType
import resonant.lib.network.handle.{TPacketReceiver, TPacketSender}
import resonant.lib.prefab.tile.TileInventory
import resonant.lib.prefab.tile.spatial.SpatialBlock
import resonant.lib.transform.vector.Vector3
import resonant.lib.utility.inventory.InventoryUtility
import resonant.lib.wrapper.ByteBufWrapper._
import resonantinduction.core.Reference
import resonantinduction.mechanical.mech.gear.ItemHandCrank

class TileMillstone extends TileInventory(Material.rock) with TPacketSender with TPacketReceiver
{
  private var grindCount: Int = 0

  //Constructor
  maxSlots = 1
  setTextureName(Reference.prefix + "millstone_side")

  override def onInventoryChanged
  {
    grindCount = 0
    worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
  }

  def doGrind(spawnPos: Vector3)
  {
    val outputs = MachineRecipes.INSTANCE.getOutput(RecipeType.GRINDER.name, getStackInSlot(0))

    if (outputs.length > 0)
    {
      grindCount += 1
      if (grindCount > 20)
      {
        for (res <- outputs)
        {
          InventoryUtility.dropItemStack(worldObj, spawnPos, res.getItemStack.copy)
        }
        decrStackSize(0, 1)
        onInventoryChanged
      }
    }
  }

  override def canUpdate: Boolean =
  {
    return false
  }

  override def isItemValidForSlot(i: Int, itemStack: ItemStack): Boolean =
  {
    return MachineRecipes.INSTANCE.getOutput(RecipeType.GRINDER.name, itemStack).length > 0
  }

  override def canStore(stack: ItemStack, slot: Int, side: ForgeDirection): Boolean =
  {
    return true
  }

  @SideOnly(Side.CLIENT)
  override def registerIcons(iconReg: IIconRegister)
  {
    SpatialBlock.icon.put("millstone_side", iconReg.registerIcon(Reference.prefix + "millstone_side"))
    SpatialBlock.icon.put("millstone_top", iconReg.registerIcon(Reference.prefix + "millstone_top"))
  }

  @SideOnly(Side.CLIENT)
  override def getIcon(side: Int, meta: Int): IIcon =
  {
    if (side == 0 || side == 1)
    {
      return SpatialBlock.icon.get("millstone_top")
    }
    return SpatialBlock.icon.get("millstone_side")
  }

  override def click(player: EntityPlayer)
  {
    if (!world.isRemote)
    {
      val output: ItemStack = getStackInSlot(0)
      if (output != null)
      {
        InventoryUtility.dropItemStack(world, new Vector3(player), output, 0)
        setInventorySlotContents(0, null)
      }
      onInventoryChanged
    }
  }

  override def use(player: EntityPlayer, hitSide: Int, hit: Vector3): Boolean =
  {
    val current: ItemStack = player.inventory.getCurrentItem
    val output: ItemStack = getStackInSlot(0)
    if (current != null && current.getItem.isInstanceOf[ItemHandCrank])
    {
      if (output != null)
      {
        doGrind(new Vector3(player))
        player.addExhaustion(0.3f)
        return true
      }
    }
    if (output != null)
    {
      InventoryUtility.dropItemStack(world, new Vector3(player), output, 0)
      setInventorySlotContents(0, null)
    }
    else if (current != null && isItemValidForSlot(0, current))
    {
      setInventorySlotContents(0, current)
      player.inventory.setInventorySlotContents(player.inventory.currentItem, null)
    }
    world.markBlockForUpdate(xi, yi, zi)
    return false
  }

  /**
   * Packets
   */
  /**
   * Override this method
   * Be sure to super this method or manually write the ID into the packet when sending
   */
  override def write(buf: ByteBuf, id: Int)
  {
    super.write(buf, id)
    buf <<<< writeToNBT
  }

  override def read(buf: ByteBuf, id: Int, packetType: PacketType)
  {
    super.read(buf, id, packetType)
    buf >>>> readFromNBT
  }
}
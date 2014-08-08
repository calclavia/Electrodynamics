package resonantinduction.atomic.machine.thermometer

import java.util.List

import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ChatComponentText
import net.minecraft.world.World
import resonant.content.prefab.itemblock.ItemBlockSaved
import resonant.lib.utility.LanguageUtility
import resonant.lib.utility.nbt.NBTUtility
import resonant.lib.wrapper.WrapList._
import universalelectricity.core.transform.vector.Vector3

/** Handheld thermometer */
object ItemBlockThermometer {
  final val ENERGY_CONSUMPTION: Int = 1000
}

class ItemBlockThermometer(block: Block) extends ItemBlockSaved(block: Block) {

  override def addInformation(itemStack: ItemStack, player: EntityPlayer, par3List: List[_], par4: Boolean) {
    super.addInformation(itemStack, player, par3List, par4)
    val coord: Vector3 = getSavedCoord(itemStack)
    if (coord != null) {
      par3List.add("\uaa74" + LanguageUtility.getLocal("tooltip.trackingTemperature"))
      par3List.add("X: " + coord.xi + ", Y: " + coord.yi + ", Z: " + coord.zi)
    }
    else {
      par3List.add("\u00a74" + LanguageUtility.getLocal("tooltip.notTrackingTemperature"))
    }
  }

  def setSavedCoords(itemStack: ItemStack, position: Vector3) {
    val nbt: NBTTagCompound = NBTUtility.getNBTTagCompound(itemStack)
    if (position != null) {
      nbt.setTag("trackCoordinate", position.writeNBT(new NBTTagCompound))
    }
    else {
      nbt.removeTag("trackCoordinate")
    }
  }

  def getSavedCoord(itemStack: ItemStack): Vector3 = {
    val nbt: NBTTagCompound = NBTUtility.getNBTTagCompound(itemStack)
    if (nbt.hasKey("trackCoordinate")) {
      return new Vector3(nbt.getCompoundTag("trackCoordinate"))
    }
    return null
  }

  override def onItemRightClick(itemStack: ItemStack, world: World, player: EntityPlayer): ItemStack = {
    setSavedCoords(itemStack, null)
    if (!world.isRemote) {
      player.addChatMessage(new ChatComponentText("Cleared tracking coordinate."))
    }
    return itemStack
  }

  override def onItemUse(itemStack: ItemStack, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, par7: Int, par8: Float, par9: Float, par10: Float): Boolean = {
    if (player.isSneaking) {
      if (!world.isRemote) {
        setSavedCoords(itemStack, new Vector3(x, y, z))
        player.addChatMessage(new ChatComponentText("Tracking coordinate: " + x + ", " + y + ", " + z))
      }
      return true
    }
    return super.onItemUse(itemStack, player, world, x, y, z, par7, par8, par9, par10)
  }
}
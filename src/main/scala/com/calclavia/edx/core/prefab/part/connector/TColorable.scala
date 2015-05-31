package com.calclavia.edx.core.prefab.part.connector

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import com.calclavia.edx.core.util.ColorUtil
import ColorUtil
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.MovingObjectPosition

/**
 * Trait applied to objects that can associates with a color.
 * @author Calclavia
 */
object TColorable
{
  val defaultColor = 15
}

trait TColorable extends PartAbstract
{
  var colorID = TColorable.defaultColor

  /**
   * Gets the Minecraft color code
   * @return An integer
   */
  def getColor = colorID

  /**
   * Changes the wire's color.
   */
  override def activate(player: EntityPlayer, part: MovingObjectPosition, itemStack: ItemStack): Boolean =
  {
    if (itemStack != null)
    {
      val dyeColor = ColorUtil.isDye(itemStack)

      if (dyeColor != -1)
      {
        if (!player.capabilities.isCreativeMode)
        {
          player.inventory.decrStackSize(player.inventory.currentItem, 1)
        }

        setColor(dyeColor)
        return true
      }
    }

    return false
  }

  def setColor(dye: Int)
  {
    colorID = dye

    if (!world.isRemote)
    {
      tile.notifyPartChange(this)
      onPartChanged(this)
      sendPacket(2)
    }
  }

  override def write(packet: MCDataOutput, id: Int)
  {
    if (id == 0 || id == 2)
      packet.writeByte(colorID.toByte)
  }

  override def read(packet: MCDataInput, id: Int)
  {
    if (id == 0 || id == 2)
      colorID = packet.readByte()
  }

  override def save(nbt: NBTTagCompound)
  {
    super.save(nbt)
    nbt.setInteger("dyeID", colorID)
  }

  override def load(nbt: NBTTagCompound)
  {
    super.load(nbt)
    this.colorID = nbt.getInteger("dyeID")
  }
}
package resonantinduction.core.prefab.part.connector

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.MovingObjectPosition
import resonantinduction.core.util.ResonantUtil

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

  def setColor(dye: Int)
  {
    colorID = dye

    if (!world.isRemote)
    {
      tile.notifyPartChange(this)
      onPartChanged(this)
      sendColorUpdate()
    }
  }

  def sendColorUpdate()
  {
    //tile.getWriteStream(this).writeByte(2).writeInt(this.colorID)
  }

  /**
   * Changes the wire's color.
   */
  override def activate(player: EntityPlayer, part: MovingObjectPosition, itemStack: ItemStack): Boolean =
  {
    if (itemStack != null)
    {
      val dyeColor = ResonantUtil.isDye(itemStack)

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

  override def write(packet: MCDataOutput, id: Int)
  {
    super.write(packet, id)

    if (id == 0 || id == 2)
      packet.writeByte(colorID.toByte)
  }

  override def read(packet: MCDataInput, packetID: Int)
  {
    packetID match
    {
      case 0 => colorID = packet.readByte
      case 2 =>
        colorID = packet.readInt()
        tile.markRender()
    }
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
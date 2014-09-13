package resonantinduction.core.prefab.part

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.multipart.TMultiPart
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.MovingObjectPosition
import resonantinduction.core.util.ResonantUtil

/**
 * Trait applied to objects that can associates with a color.
 * @author Calclavia
 */
trait TColorable[M] extends TMultiPart with TPart
{
  val defaultColor = 15
  var colorID = defaultColor

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
    tile.getWriteStream(this).writeByte(2).writeInt(this.colorID)
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

  override def readDesc(packet: MCDataInput)
  {
    colorID = packet.readByte
  }

  override def writeDesc(packet: MCDataOutput)
  {
    packet.writeByte(colorID.toByte)
  }

  def read(packet: MCDataInput, packetID: Int)
  {
    if (packetID == 2)
    {
      colorID = packet.readInt
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
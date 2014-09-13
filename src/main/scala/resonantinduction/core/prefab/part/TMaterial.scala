package resonantinduction.core.prefab.part

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.multipart.TMultiPart
import net.minecraft.item.Item
import net.minecraft.nbt.NBTTagCompound

/**
 * Trait applied to objects that can associates with a material.
 * @author Calclavia
 */
trait TMaterial[M] extends TMultiPart
{
  var material: M = _

  def setMaterial(i: Int)

  def getMaterialID: Int

  override def readDesc(packet: MCDataInput)
  {
    setMaterial(packet.readByte)
  }

  override def writeDesc(packet: MCDataOutput)
  {
    packet.writeByte(getMaterialID.toByte)
  }

  override def save(nbt: NBTTagCompound)
  {
    super.save(nbt)
    nbt.setInteger("typeID", getMaterialID)
  }

  override def load(nbt: NBTTagCompound)
  {
    super.load(nbt)
    setMaterial(nbt.getInteger("typeID"))
  }

}
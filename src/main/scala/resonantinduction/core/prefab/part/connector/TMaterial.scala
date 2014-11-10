package resonantinduction.core.prefab.part.connector

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import net.minecraft.nbt.NBTTagCompound

/**
 * Trait applied to objects that can associates with a material.
 * @author Calclavia
 */
trait TMaterial[M] extends PartAbstract
{
  var material: M = _

  def setMaterial(i: Int)

  def getMaterialID: Int

  override def read(packet: MCDataInput, id: Int)
  {
    if (id == 0)
      setMaterial(packet.readByte)
  }

  override def write(packet: MCDataOutput, id: Int)
  {
    if (id == 0)
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
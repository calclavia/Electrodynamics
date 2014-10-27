package mffs.field.mode

import net.minecraft.nbt.NBTTagCompound
import resonant.lib.transform.vector.Vector3

/**
 * @author Calclavia
 */
abstract class Schematic
{
  def getExterior: Set[Vector3]

  def getInterior: Set[Vector3]

  def load(nbt: NBTTagCompound)
  {
  }

  def save(nbt: NBTTagCompound)
  {

  }
}

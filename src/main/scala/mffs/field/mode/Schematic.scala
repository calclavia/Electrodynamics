package mffs.field.mode

import net.minecraft.nbt.NBTTagCompound
import nova.core.util.transform.Vector3d

/**
 * @author Calclavia
 */
abstract class Schematic
{
  def getExterior: Set[Vector3d]

  def getInterior: Set[Vector3d]

  def load(nbt: NBTTagCompound)
  {
  }

  def save(nbt: NBTTagCompound)
  {

  }
}

package mffs.field.mode

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

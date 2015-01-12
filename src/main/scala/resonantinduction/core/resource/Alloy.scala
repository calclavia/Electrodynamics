package resonantinduction.core.resource

import net.minecraft.nbt.NBTTagCompound
import resonant.lib.utility.nbt.ISaveObj

/**
 * A class that stores alloy objects
 * @author Calclavia
 */
class Alloy(val max: Int) extends ISaveObj
{
  var mixture = Map.empty[String, Int]

  override def save(nbt: NBTTagCompound)
  {

  }

  override def load(nbt: NBTTagCompound)
  {

  }
}

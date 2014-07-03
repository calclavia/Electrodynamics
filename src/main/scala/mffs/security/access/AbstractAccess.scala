package mffs.security.access

import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import resonant.lib.access.{Permissions, Permission}

/**
 * The abstract access class.
 * @author Calclavia
 */
abstract class AbstractAccess
{
  var permissions = Set[Permission]

  def fromNBT(nbt: NBTTagCompound)
  {
    this()
    val permList = nbt.getTagList("permissions", 10)
    permissions = (0 until permList.tagCount()).map(Permissions.find(permList.getStringTagAt()))
  }

  def toNBT: NBTTagCompound =
  {
    val permList = new NBTTagList()
    permissions.foreach(x => permList.appendTag(x.toString))
    nbt.setTag("permissions", permList)

    return nbt
  }
}

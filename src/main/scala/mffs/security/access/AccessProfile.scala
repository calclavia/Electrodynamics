package mffs.security.access

import com.mojang.authlib.GameProfile
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import resonant.lib.access.Permission

import scala.collection.mutable

/**
 * Access Management Classes
 * @author Calclavia
 */

class AccessProfile
{
  var groups = new mutable.HashSet[AccessGroup]()

  def this(nbt: NBTTagCompound)
  {
    this()
    val nbtList = nbt.getTagList("groups", 10)
    groups = (0 until nbtList.tagCount()).map(new AccessGroup(nbtList.getCompoundTagAt(_)))
  }

  def toNBT: NBTTagCompound =
  {
    val nbt = new NBTTagCompound()
    val nbtList = new NBTTagList()
    groups.foreach(group => nbtList.appendTag(user.toNBT))
    nbt.setTag("groups", nbtList)
    return nbt
  }

  def hasPermission(profile: GameProfile, permission: Permission): Boolean =
  {
    return groups.exists(_.hasPermission(profile, permission))
  }
}







package mffs.security.access

import com.mojang.authlib.GameProfile
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import resonant.lib.access.Permission

/**
 * Access Management Classes
 * @author Calclavia
 */

class AccessProfile
{
  var groups = Set.empty[AccessGroup]

  def this(nbt: NBTTagCompound)
  {
    this()
    val nbtList = nbt.getTagList("groups", 10)
    groups = ((0 until nbtList.tagCount()) map (i => new AccessGroup(nbtList.getCompoundTagAt(i)))).toSet
  }

  def toNBT: NBTTagCompound =
  {
    val nbt = new NBTTagCompound()
    val nbtList = new NBTTagList()
    groups.foreach(group => nbtList.appendTag(group.toNBT))
    nbt.setTag("groups", nbtList)
    return nbt
  }

  def hasPermission(profile: GameProfile, permission: Permission): Boolean =
  {
    return groups.exists(_.hasPermission(profile, permission))
  }
}







package mffs.security.access

import com.mojang.authlib.GameProfile
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import resonant.lib.access.Permission

class AccessGroup extends AbstractAccess
{
  var users = Set.empty[AccessUser]

  def this(nbt: NBTTagCompound)
  {
    this()
    val nbtList = nbt.getTagList("users", 10)
    users = ((0 until nbtList.tagCount()) map (i => new AccessUser(nbtList.getCompoundTagAt(i)))).toSet
    fromNBT(nbt)
  }

  def toNBT: NBTTagCompound =
  {
    val nbt = super.toNBT

    val userList = new NBTTagList()
    users.foreach(x => userList.appendTag(x.toNBT))
    nbt.setTag("users", userList)
    return nbt
  }

  def hasPermission(profile: GameProfile, permission: Permission): Boolean =
  {
    return users.exists(_.profile.equals(profile)) && (permissions.contains(permission) || users.exists(_.hasPermission(permission)))
  }
}
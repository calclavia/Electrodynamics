package mffs.security.access

import java.util.UUID

import com.mojang.authlib.GameProfile
import net.minecraft.nbt.NBTTagCompound
import resonant.lib.access.Permission

class AccessUser(val profile: GameProfile) extends AbstractAccess
{
  def this(nbt: NBTTagCompound)
  {
    this(new GameProfile(UUID.fromString(nbt.getString("UUID")), nbt.getString("username")))
    fromNBT(nbt)
  }

  def toNBT: NBTTagCompound =
  {
    val nbt = super.toNBT
    nbt.setString("UUID", profile.getId.toString)
    nbt.setString("username", profile.getName)
    return nbt
  }

  def hasPermission(permission: Permission): Boolean = permissions.contains(permission)
}
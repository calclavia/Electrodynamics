package mffs.security.access

import com.mojang.authlib.GameProfile
import net.minecraft.nbt.NBTTagCompound
import resonant.lib.access.Permission

class AccessUser(val profile: GameProfile) extends AbstractAccess
{
  def this(nbt: NBTTagCompound)
  {
    this(proifle = new GameProfile(nbt.getString("UUID"), nbt.getString("username")))
    fromNBT(nbt)
  }

  def toNBT: NBTTagCompound =
  {
    val nbt = super.toNBT
    nbt.setString("UUID", profile.getId)
    nbt.setString("username", profile.getName)
    return nbt
  }

  def hasPermission(permission: Permission): Boolean = permissions.exists(permission)
}
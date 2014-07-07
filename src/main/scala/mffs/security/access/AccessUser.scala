package mffs.security.access

import java.util.UUID

import com.mojang.authlib.GameProfile
import net.minecraft.nbt.NBTTagCompound
import resonant.lib.access.Permission
import resonant.lib.utility.nbt.NBTUtility

class AccessUser(val profile: GameProfile) extends AbstractAccess
{
  def this(nbt: NBTTagCompound)
  {
    this(NBTUtility.loadProfile(nbt))
    fromNBT(nbt)
  }

  override def toNBT: NBTTagCompound =  NBTUtility.saveProfile(super.toNBT, profile)

  def hasPermission(permission: Permission): Boolean = permissions.contains(permission)
}
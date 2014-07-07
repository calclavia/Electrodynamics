package mffs.item.card

import mffs.security.access.MFFSPermissions
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import resonant.lib.access.Permission
import resonant.lib.utility.nbt.NBTUtility

/**
 * @author Calclavia
 */
class ItemCardPermission extends ItemCard
{
  private final val NBT_PREFIX = "mffs_permission_"

  def hasPermission(itemStack: ItemStack, permission: Permission): Boolean =
  {
    val nbt = NBTUtility.getNBTTagCompound(itemStack)
    return nbt.getBoolean(NBT_PREFIX + permission.id)
  }

  def addPermission(itemStack: ItemStack, permission: Permission): Boolean =
  {
    val nbt = NBTUtility.getNBTTagCompound(itemStack)
    nbt.setBoolean(NBT_PREFIX + permission.id, true)
    return false
  }

  def removePermission(itemStack: ItemStack, permission: Permission): Boolean =
  {
    val nbt = NBTUtility.getNBTTagCompound(itemStack)
    nbt.setBoolean(NBT_PREFIX + permission.id, false)
    return false
  }
}

package mffs.item.card

import java.util.{Set => JSet}

import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagList, NBTTagString}
import resonant.lib.access.{Permission, Permissions}
import resonant.lib.utility.nbt.NBTUtility

import scala.collection.convert.wrapAll._
import scala.collection.mutable

/**
 * @author Calclavia
 */
class ItemCardPermission extends ItemCard
{
  private final val nbtPermission = "permissions"

  def getPermissions(itemStack: ItemStack): JSet[Permission] =
  {
    val nbt = NBTUtility.getNBTTagCompound(itemStack)
    val nbtList = nbt.getTagList(nbtPermission, 9)
    return ((0 until nbtList.tagCount) map (i => Permissions.find(nbtList.getStringTagAt(i))) filter (_ != null)).to[mutable.Set]
  }

  def setPermissions(itemStack: ItemStack, permissions: Permission*)
  {
    val nbt = NBTUtility.getNBTTagCompound(itemStack)
    val nbtList = new NBTTagList
    permissions foreach (permission => nbtList.appendTag(new NBTTagString(permission.toString)))
    nbt.setTag(nbtPermission, nbtList)
  }

  def hasPermission(itemStack: ItemStack, permission: Permission*): Boolean =
  {
    return getPermissions(itemStack).containsAll(permission)
  }

  def addPermission(itemStack: ItemStack, permission: Permission*)
  {
    setPermissions(itemStack, (getPermissions(itemStack) ++ permission).toSeq: _*)
  }

  def removePermission(itemStack: ItemStack, permission: Permission*)
  {
    setPermissions(itemStack, (getPermissions(itemStack) -- permission).toSeq: _*)
  }
}

package mffs.security.card

import java.util.{Set => JSet}

import mffs.item.card.ItemCard
import net.minecraft.item.ItemStack
import resonant.api.mffs.card.IAccessCard
import resonant.lib.access.scala.AbstractAccess

/**
 * @author Calclavia
 */
abstract class ItemCardAccess extends ItemCard with IAccessCard
{
  def setAccess(itemStack: ItemStack, access: AbstractAccess) = itemStack.setTagCompound(access.toNBT)
}

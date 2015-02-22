package mffs.security.card

import java.util.{Set => JSet}

import mffs.item.card.ItemCard

/**
 * @author Calclavia
 */
abstract class ItemCardAccess extends ItemCard with IAccessCard
{
	def setAccess(Item: Item, access: AbstractAccess) = Item.setTagCompound(access.toNBT)
}

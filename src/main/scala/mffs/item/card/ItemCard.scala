package mffs.item.card

import mffs.api.card.Card
import mffs.base.CategoryMFFS
import nova.core.item.Item

class ItemCard extends Item with Card with CategoryMFFS {
	override def getMaxCount: Int = 1

	override def getID: String = "cardBlank"
}
package mffs.item.card

import mffs.api.card.Card
import nova.core.item.Item

class ItemCard extends Item with Card {
	override def getMaxCount: Int = 1

	override def getID: String = "cardBlank"
}
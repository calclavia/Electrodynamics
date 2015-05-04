package mffs.item.card

import com.resonant.core.prefab.modcontent.AutoItemTexture
import mffs.api.card.Card
import mffs.base.CategoryMFFS
import nova.core.item.Item

class ItemCard extends Item with Card with CategoryMFFS with AutoItemTexture {
	override def getMaxCount: Int = 1

	override def getID: String = "cardBlank"
}
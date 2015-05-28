package com.calclavia.edx.mffs.item.card

import com.calclavia.edx.mffs.api.card.Card
import com.calclavia.edx.mffs.base.CategoryMFFS
import com.resonant.core.prefab.modcontent.AutoItemTexture
import nova.core.item.Item

class ItemCard extends Item with Card with AutoItemTexture {

	add(new CategoryMFFS)

	override def getMaxCount: Int = 1

	override def getID: String = "cardBlank"
}
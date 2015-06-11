package com.calclavia.edx.optics.item.card

import com.calclavia.edx.optics.api.card.Card
import com.calclavia.edx.optics.component.CategoryMFFS
import nova.core.item.Item
import nova.scala.modcontent.AutoItemTexture

class ItemCard extends Item with Card with AutoItemTexture {

	add(new CategoryMFFS)

	override def getMaxCount: Int = 1

	override def getID: String = "cardBlank"
}
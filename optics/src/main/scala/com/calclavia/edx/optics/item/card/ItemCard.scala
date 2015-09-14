package com.calclavia.edx.optics.item.card

import com.calclavia.edx.core.CategoryEDX
import com.calclavia.edx.optics.api.card.Card
import nova.core.item.Item
import nova.scala.modcontent.AutoItemTexture

class ItemCard extends Item with Card with AutoItemTexture {

	components.add(new CategoryEDX)

	override def getMaxCount: Int = 1
}
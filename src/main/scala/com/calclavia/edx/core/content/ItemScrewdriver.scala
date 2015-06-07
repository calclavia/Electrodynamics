package com.calclavia.edx.core.content

import java.util.Optional

import com.calclavia.edx.core.CoreContent
import nova.core.component.Category
import nova.core.item.Item
import nova.core.render.texture.ItemTexture

class ItemScrewdriver extends Item {
	add(new Category("tools"))

	override def getID: String = "screwdriver"

	override def getTexture: Optional[ItemTexture] = Optional.of(CoreContent.textureScrewdriver)
}
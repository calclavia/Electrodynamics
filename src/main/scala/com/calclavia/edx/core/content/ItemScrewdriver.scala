package com.calclavia.edx.core.content

import com.calclavia.edx.core.CoreContent
import nova.core.component.Category
import nova.core.component.renderer.ItemRenderer
import nova.core.item.Item

class ItemScrewdriver extends Item {
	add(new Category("tools"))
	add(new ItemRenderer())
		.setTexture(CoreContent.textureScrewdriver)
}
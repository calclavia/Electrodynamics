package com.calclavia.edx.optics.field.shape

import com.calclavia.edx.core.CategoryEDX
import com.calclavia.edx.optics.api.modules.StructureProvider
import com.calclavia.edx.optics.content.OpticsTextures
import nova.core.component.renderer.ItemRenderer
import nova.core.item.Item

abstract class ItemShape extends Item with StructureProvider {

	val renderer = add(new ItemRenderer(this))
	val category = add(new CategoryEDX)

	override def getMaxCount: Int = 1

	def getFortronCost(amplifier: Float): Float = 8
}
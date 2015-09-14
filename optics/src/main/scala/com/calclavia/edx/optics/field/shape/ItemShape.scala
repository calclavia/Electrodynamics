package com.calclavia.edx.optics.field.shape

import com.calclavia.edx.core.CategoryEDX
import com.calclavia.edx.optics.api.modules.StructureProvider
import nova.core.component.renderer.ItemRenderer
import nova.core.item.Item

abstract class ItemShape extends Item with StructureProvider {

	val renderer = components.add(new ItemRenderer(this))
	val category = components.add(new CategoryEDX)

	override def getMaxCount: Int = 1

	def getFortronCost(amplifier: Float): Float = 8
}
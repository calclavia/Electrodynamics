package com.calclavia.edx.optics.field.shape

import com.calclavia.edx.optics.api.modules.StructureProvider
import com.calclavia.edx.optics.component.CategoryEDXOptics
import nova.core.item.Item

abstract class ItemShape extends Item with StructureProvider {

	add(new CategoryEDXOptics)

	override def getMaxCount: Int = 1

	def getFortronCost(amplifier: Float): Float = 8
}
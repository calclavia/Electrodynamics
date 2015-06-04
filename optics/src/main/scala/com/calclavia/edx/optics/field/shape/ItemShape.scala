package com.calclavia.edx.optics.field.shape

import com.calclavia.edx.optics.api.modules.StructureProvider
import nova.core.item.Item

abstract class ItemShape extends Item with StructureProvider {

	override def getMaxCount: Int = 1

	def getFortronCost(amplifier: Float): Float = 8
}
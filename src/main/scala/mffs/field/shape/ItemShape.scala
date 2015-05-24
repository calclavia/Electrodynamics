package mffs.field.shape

import mffs.api.modules.StructureProvider
import nova.core.item.Item

abstract class ItemShape extends Item with StructureProvider {

	override def getMaxCount: Int = 1

	def getFortronCost(amplifier: Float): Float = 8
}
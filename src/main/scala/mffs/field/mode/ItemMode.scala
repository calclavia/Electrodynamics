package mffs.field.mode

import mffs.api.modules.StructureProvider
import nova.core.item.Item

abstract class ItemMode extends Item with StructureProvider {

	override def getMaxCount: Int = 1

	def getFortronCost(amplifier: Float): Float = 8
}
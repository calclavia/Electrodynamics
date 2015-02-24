package mffs.field.mode

import mffs.api.modules.ProjectorMode
import nova.core.item.Item

abstract class ItemMode extends Item with ProjectorMode {

	override def getMaxCount: Int = 1

	def getFortronCost(amplifier: Float): Float = 8
}
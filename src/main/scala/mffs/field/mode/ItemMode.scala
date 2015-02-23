package mffs.field.mode

import mffs.api.modules.IProjectorMode
import nova.core.item.Item
import nova.core.util.transform.Vector3d

abstract class ItemMode extends Item with IProjectorMode {

	override def getMaxCount: Int = 1

	def render(projector: IProjector, x: Double, y: Double, z: Double, f: Float, ticks: Long) {
	}

	def isInField(projector: IFieldMatrix, position: Vector3d): Boolean = {
		return false
	}

	def getFortronCost(amplifier: Float): Float = {
		return 8
	}
}
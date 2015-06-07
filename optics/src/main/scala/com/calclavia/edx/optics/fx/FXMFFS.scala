package com.calclavia.edx.optics.fx

import nova.core.entity.Entity
import nova.scala.util.ExtendedUpdater

/**
 * @author Calclavia
 */
abstract class FXMFFS extends Entity with ExtendedUpdater {
	protected var controller: IEffectController = null

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		if (this.controller != null) {
			if (!this.controller.canContinueEffect) {
				world.removeEntity(this)
			}
		}
	}

	def setController(controller: IEffectController): FXMFFS = {
		this.controller = controller
		return this
	}
}
package mffs.render.fx

import com.resonant.core.prefab.block.Updater
import nova.core.entity.Entity

/**
 * @author Calclavia
 */
abstract class FXMFFS extends Entity with Updater {
	protected var controller: IEffectController = null

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		if (this.controller != null) {
			if (!this.controller.canContinueEffect) {
				world.destroyEntity(this)
			}
		}
	}

	def setController(controller: IEffectController): FXMFFS = {
		this.controller = controller
		return this
	}
}
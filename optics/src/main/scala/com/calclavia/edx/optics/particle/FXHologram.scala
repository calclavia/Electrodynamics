package com.calclavia.edx.optics.particle

import com.calclavia.edx.optics.content.OpticsTextures
import com.resonant.lib.WrapFunctions._
import nova.core.component.renderer.DynamicRenderer
import nova.core.entity.component.RigidBody
import nova.core.render.Color
import nova.core.render.model.{BlockModelUtil, Model}
import nova.core.util.transform.vector.Vector3d

import scala.collection.convert.wrapAll._
class FXHologram(color: Color, maxAge: Double) extends FXMFFS {
	var age = 0d
	private var targetPosition: Vector3d = null

	add(new DynamicRenderer())
		.setOnRender(
	    (model: Model) => {
			model.scale(1.01, 1.01, 1.01)

			var op = 0.5

			if (maxAge - age <= 4) {
				op = 0.5f - (5 - (maxAge - age)) * 0.1F
			}

			//		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F)
			//		RenderUtility.enableBlending
			BlockModelUtil.drawCube(model)
			model.bindAll(OpticsTextures.hologram)
			model.faces.foreach(_.vertices.foreach(_.setColor(color.alpha((op * 255).toInt))))
			//		RenderUtility.disableBlending
		}
		)

	override def getID: String = "hologram"

	/**
	 * The target the hologram is going to translate to.
	 *
	 * @param targetPosition
	 * @return
	 */
	def setTarget(targetPosition: Vector3d): FXHologram = {
		this.targetPosition = targetPosition
		get(classOf[RigidBody]).setVelocity((targetPosition - position) / maxAge)
		return this
	}

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		age += deltaTime

		if (age > maxAge) {
			world.removeEntity(this)
		}
	}

}
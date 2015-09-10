package com.calclavia.edx.optics.fx

import com.calclavia.edx.optics.content.OpticsTextures
import nova.core.component.renderer.DynamicRenderer
import nova.core.entity.component.RigidBody
import nova.core.render.Color
import nova.core.render.model.{Model, MeshModel}
import nova.core.render.pipeline.BlockRenderStream
import nova.scala.wrapper.FunctionalWrapper._
import nova.scala.wrapper.VectorWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import scala.collection.convert.wrapAll._

class FXHologram(color: Color, maxAge: Double) extends FXMFFS {
	var age = 0d
	private var targetPosition: Vector3D = null

	add(new DynamicRenderer())
		.onRender(
			(model: Model) => {
				model.matrix.scale(1.01, 1.01, 1.01)

				var op = 0.5

				if (maxAge - age <= 4) {
					op = 0.5f - (5 - (maxAge - age)) * 0.1F
				}

				//OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F)
				//RenderUtility.enableBlending
				val cube = BlockRenderStream.drawCube(new MeshModel())
				cube.bindAll(OpticsTextures.hologram)
				cube.faces.foreach(_.vertices.foreach(_.color = (color.alpha((op * 255).toInt))))
				model.addChild(cube)
				//RenderUtility.disableBlending
			}
		)

	override def getID: String = "hologram"

	/**
	 * The target the hologram is going to translate to.
	 *
	 * @param targetPosition
	 * @return
	 */
	def setTarget(targetPosition: Vector3D): FXHologram = {
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
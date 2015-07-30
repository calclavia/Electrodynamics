package com.calclavia.edx.optics.fx

import com.calclavia.edx.optics.content.OpticsTextures
import nova.core.component.renderer.DynamicRenderer
import nova.core.render.Color
import nova.core.render.model.{Model, VertexModel}
import nova.core.render.pipeline.BlockRenderStream
import nova.scala.util.ExtendedUpdater
import nova.scala.wrapper.FunctionalWrapper._

import scala.beans.BeanProperty
import scala.collection.convert.wrapAll._

class FXHologramProgress(@BeanProperty var color: Color, maxAge: Double) extends FXMFFS with ExtendedUpdater {
	var age = 0d

	add(new DynamicRenderer())
		.onRender(
			(model: Model) => {
				//		GL11.glPushMatrix
				val completion = age / maxAge
				model.matrix.scale(1.01, 1.01, 1.01)
				model.matrix.translate(0, (completion - 1) / 2, 0)
				model.matrix.scale(1, completion, 1)

				var op = 0.5

				if (maxAge - age <= 4) {
					op = 0.5f - (5 - (maxAge - age)) * 0.1F
				}

				//OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F)
				//RenderUtility.enableBlending
				val cube = BlockRenderStream.drawCube(new VertexModel())
				cube.bindAll(OpticsTextures.hologram)
				cube.faces.foreach(_.vertices.foreach(_.color = (color.alpha((op * 255).toInt))))
				model.addChild(cube)
				//RenderUtility.disableBlending
			}
		)

	override def getID: String = "hologramMoving"

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		age += deltaTime

		if (age > maxAge) {
			world.removeEntity(this)
		}
	}

}
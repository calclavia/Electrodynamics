package mffs.particle

import com.resonant.core.prefab.block.Updater
import mffs.content.Textures
import nova.core.block.components.DynamicRenderer
import nova.core.render.Color
import nova.core.render.model.{BlockModelUtil, Model}

import scala.beans.BeanProperty
import scala.collection.convert.wrapAll._

class FXHologramProgress(@BeanProperty var color: Color, maxAge: Double) extends FXMFFS with Updater with DynamicRenderer {
	var age = 0d

	override def getID: String = "hologramMoving"

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		age += deltaTime

		if (age > maxAge) {
			world.removeEntity(this)
		}
	}

	override def renderDynamic(model: Model) {
		//		GL11.glPushMatrix
		val completion = age / maxAge
		model.scale(1.01, 1.01, 1.01)
		model.translate(0, (completion - 1) / 2, 0)
		model.scale(1, completion, 1)

		var op = 0.5

		if (maxAge - age <= 4) {
			op = 0.5f - (5 - (maxAge - age)) * 0.1F
		}

		//		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F)
		//		RenderUtility.enableBlending
		BlockModelUtil.drawCube(model)
		model.bindAll(Textures.hologram)
		model.faces.foreach(_.vertices.foreach(_.setColor(color.alpha((op * 255).toInt))))
		//		RenderUtility.disableBlending
	}
}
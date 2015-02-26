package mffs.render.fx

import com.resonant.core.prefab.block.Updater
import mffs.content.Textures
import nova.core.entity.Entity
import nova.core.render.Color
import nova.core.render.model.{BlockModelUtil, Model}
import nova.core.util.transform.Vector3d

import scala.collection.convert.wrapAll._

class FXHologramProgress(pos: Vector3d, color: Color, maxAge: Double) extends Entity with Updater {
	var prevPos = pos
	var age = 0d

	setPosition(pos)

	override def getID: String = "hologramMoving"

	override def update(deltaTime: Double) {
		super.update(deltaTime)
		prevPos = position

		age += deltaTime

		if (age > maxAge) {
			world.destroyEntity(this)
		}
	}

	override def render(model: Model) {
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
package com.calclavia.edx.optics.fx

import com.calclavia.edx.optics.content.OpticsTextures
import com.resonant.lib.WrapFunctions._
import nova.core.component.renderer.DynamicRenderer
import nova.core.render.Color
import nova.core.render.model.{BlockModelUtil, Model}
import nova.scala.util.ExtendedUpdater

import scala.beans.BeanProperty
import scala.collection.convert.wrapAll._

class FXHologramProgress(@BeanProperty var color: Color, maxAge: Double) extends FXMFFS with ExtendedUpdater {
	var age = 0d

	add(new DynamicRenderer())
		.setOnRender(
	    (model: Model) => {
		    //		GL11.glPushMatrix
		    val completion = age / maxAge
		    model.scale(1.01, 1.01, 1.01)
		    model.translate(0, (completion - 1) / 2, 0)
		    model.scale(1, completion, 1)

		    var op = 0.5

		    if (maxAge - age <= 4) {
			    op = 0.5f - (5 - (maxAge - age)) * 0.1F
		    }

		    //OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F)
		    //RenderUtility.enableBlending
		    BlockModelUtil.drawCube(model)
			model.bindAll(OpticsTextures.hologram)
		    model.faces.foreach(_.vertices.foreach(_.setColor(color.alpha((op * 255).toInt))))
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
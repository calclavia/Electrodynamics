package com.calclavia.edx.optics.field.shape

import com.calclavia.edx.optics.content.OpticsTextures
import com.resonant.core.structure.{Structure, StructureCube}
import nova.core.render.model.{BlockModelUtil, Model}
import nova.scala.wrapper.FunctionalWrapper._

class ItemShapeCube extends ItemShape {

	override def getID: String = "modeCube"

	override def getStructure: Structure = new StructureCube

	renderer.setOnRender(
		(model: Model) => {
			model.matrix.scale(0.5, 0.5, 0.5)
			BlockModelUtil.drawCube(model)
			model.bindAll(OpticsTextures.hologram)
		}
	)
}
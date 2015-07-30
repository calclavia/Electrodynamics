package com.calclavia.edx.optics.field.shape

import com.calclavia.edx.optics.content.OpticsTextures
import com.resonant.core.structure.{Structure, StructureCube}
import nova.core.render.model.{Model, VertexModel}
import nova.core.render.pipeline.BlockRenderStream
import nova.scala.wrapper.FunctionalWrapper._

class ItemShapeCube extends ItemShape {

	override def getID: String = "modeCube"

	override def getStructure: Structure = new StructureCube

	renderer.onRender(
		(model: Model) => {
			model.matrix.scale(0.5, 0.5, 0.5)
			val subModel = new VertexModel()
			BlockRenderStream.drawCube(subModel)
			subModel.bindAll(OpticsTextures.hologram)
			model.addChild(subModel)
		}
	)
}
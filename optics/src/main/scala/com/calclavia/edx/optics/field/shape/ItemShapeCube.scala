package com.calclavia.edx.optics.field.shape

import com.calclavia.edx.optics.content.OpticsTextures
import com.resonant.core.structure.{Structure, StructureCube}
import nova.core.render.model.{MeshModel, Model}
import nova.core.render.pipeline.BlockRenderPipeline
import nova.scala.wrapper.FunctionalWrapper._

class ItemShapeCube extends ItemShape {

	override def getStructure: Structure = new StructureCube

	renderer.onRender(
		(model: Model) => {
			model.matrix.scale(0.5, 0.5, 0.5)
			val subModel = new MeshModel()
			BlockRenderPipeline.drawCube(subModel)
			subModel.bindAll(OpticsTextures.hologram)
			model.addChild(subModel)
		}
	)
}
package com.calclavia.edx.optics.field.shape

import com.calclavia.edx.optics.content.OpticsTextures
import com.calclavia.edx.optics.field.structure.StructureTube
import com.resonant.core.structure.Structure
import nova.core.render.model.{MeshModel, Model}
import nova.core.render.pipeline.BlockRenderStream
import nova.scala.wrapper.FunctionalWrapper._

import scala.collection.convert.wrapAll._

class ItemShapeTube extends ItemShape {

	override def getStructure: Structure = new StructureTube

	renderer.onRender(
		(model: Model) => {
			/**
			 * Create 4 small planes
			 */
			val planeA = new MeshModel()
			planeA.matrix.scale(0.5, 0.5, 0.5)
			planeA.matrix.translate(0, 0.5, 0)
			BlockRenderStream.drawCube(planeA)

			val planeB = new MeshModel()
			planeB.matrix.scale(0.5, 0.5, 0.5)
			planeB.matrix.translate(0, -0.5, 0)
			BlockRenderStream.drawCube(planeB)

			val planeC = new MeshModel()
			planeC.matrix.scale(0.5, 0.5, 0.5)
			planeC.matrix.translate(0, 0, 0.5)
			BlockRenderStream.drawCube(planeC)

			val planeD = new MeshModel()
			planeD.matrix.scale(0.5, 0.5, 0.5)
			planeD.matrix.translate(0, 0, -0.5)
			BlockRenderStream.drawCube(planeD)

			model.children.add(planeA)
			model.children.add(planeB)
			model.children.add(planeC)
			model.children.add(planeD)
			model.children.collect { case m: MeshModel => m.bindAll(OpticsTextures.hologram) }
		}
	)
}
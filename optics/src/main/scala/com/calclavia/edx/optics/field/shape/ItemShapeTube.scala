package com.calclavia.edx.optics.field.shape

import com.calclavia.edx.optics.content.OpticsTextures
import com.calclavia.edx.optics.field.structure.StructureTube
import com.resonant.core.structure.Structure
import nova.core.render.model.{Model, VertexModel}
import nova.core.render.pipeline.BlockRenderStream
import nova.scala.wrapper.FunctionalWrapper._

import scala.collection.convert.wrapAll._

class ItemShapeTube extends ItemShape {

	override def getID: String = "modeTube"

	override def getStructure: Structure = new StructureTube

	renderer.onRender(
		(model: Model) => {
			/**
			 * Create 4 small planes
			 */
			val planeA = new VertexModel()
			planeA.matrix.scale(0.5, 0.5, 0.5)
			planeA.matrix.translate(0, 0.5, 0)
			BlockRenderStream.drawCube(planeA)

			val planeB = new VertexModel()
			planeB.matrix.scale(0.5, 0.5, 0.5)
			planeB.matrix.translate(0, -0.5, 0)
			BlockRenderStream.drawCube(planeB)

			val planeC = new VertexModel()
			planeC.matrix.scale(0.5, 0.5, 0.5)
			planeC.matrix.translate(0, 0, 0.5)
			BlockRenderStream.drawCube(planeC)

			val planeD = new VertexModel()
			planeD.matrix.scale(0.5, 0.5, 0.5)
			planeD.matrix.translate(0, 0, -0.5)
			BlockRenderStream.drawCube(planeD)

			model.children.add(planeA)
			model.children.add(planeB)
			model.children.add(planeC)
			model.children.add(planeD)
			model.children.collect { case m: VertexModel => m.bindAll(OpticsTextures.hologram) }
		}
	)
}
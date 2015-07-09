package com.calclavia.edx.optics.field.shape

import com.calclavia.edx.optics.content.OpticsTextures
import com.calclavia.edx.optics.field.structure.StructureTube
import com.resonant.core.structure.Structure
import nova.core.render.model.{BlockModelUtil, Model}
import nova.scala.wrapper.FunctionalWrapper._

class ItemShapeTube extends ItemShape {

	override def getID: String = "modeTube"

	override def getStructure: Structure = new StructureTube

	renderer.setOnRender(
		(model: Model) => {
			/**
			 * Create 4 small planes
			 */
			val planeA = new Model()
			planeA.matrix.scale(0.5, 0.5, 0.5)
			planeA.matrix.translate(0, 0.5, 0)
			BlockModelUtil.drawCube(planeA)

			val planeB = new Model()
			planeB.matrix.scale(0.5, 0.5, 0.5)
			planeB.matrix.translate(0, -0.5, 0)
			BlockModelUtil.drawCube(planeB)

			val planeC = new Model()
			planeC.matrix.scale(0.5, 0.5, 0.5)
			planeC.matrix.translate(0, 0, 0.5)
			BlockModelUtil.drawCube(planeC)

			val planeD = new Model()
			planeD.matrix.scale(0.5, 0.5, 0.5)
			planeD.matrix.translate(0, 0, -0.5)
			BlockModelUtil.drawCube(planeD)

			model.children.add(planeA)
			model.children.add(planeB)
			model.children.add(planeC)
			model.children.add(planeD)
			model.bindAll(OpticsTextures.hologram)
		}
	)
}
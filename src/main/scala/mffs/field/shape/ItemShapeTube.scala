package mffs.field.shape

import com.resonant.core.structure.Structure
import mffs.api.machine.Projector
import mffs.field.structure.StructureTube
import nova.core.render.model.{BlockModelUtil, Model}

class ItemShapeTube extends ItemShape {

	override def getID: String = "modeTube"

	override def getStructure: Structure = new StructureTube

	override def render(projector: Projector, model: Model) {
		/**
		 * Create 4 small planes 
		 */
		val planeA = new Model()
		planeA.scale(0.5, 0.5, 0.5)
		planeA.translate(0, 0.5, 0)
		BlockModelUtil.drawCube(planeA)

		val planeB = new Model()
		planeB.scale(0.5, 0.5, 0.5)
		planeB.translate(0, -0.5, 0)
		BlockModelUtil.drawCube(planeB)

		val planeC = new Model()
		planeC.scale(0.5, 0.5, 0.5)
		planeC.translate(0, 0, 0.5)
		BlockModelUtil.drawCube(planeC)

		val planeD = new Model()
		planeD.scale(0.5, 0.5, 0.5)
		planeD.translate(0, 0, -0.5)
		BlockModelUtil.drawCube(planeD)

		model.children.add(planeA)
		model.children.add(planeB)
		model.children.add(planeC)
		model.children.add(planeD)
	}
}
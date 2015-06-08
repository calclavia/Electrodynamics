package com.calclavia.edx.optics.field.shape

import com.calclavia.edx.optics.api.machine.Projector
import com.resonant.core.structure.{Structure, StructureCube}
import nova.core.render.model.{BlockModelUtil, Model}

class ItemShapeCube extends ItemShape {

	override def getID: String = "modeCube"

	override def getStructure: Structure = new StructureCube

	override def render(projector: Projector, model: Model) {
		model.matrix.scale(0.5, 0.5, 0.5)
		BlockModelUtil.drawCube(model)
	}
}
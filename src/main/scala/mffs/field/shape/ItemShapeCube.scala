package mffs.field.shape

import com.resonant.core.structure.{Structure, StructureCube}
import mffs.api.machine.Projector
import nova.core.render.model.{BlockModelUtil, Model}
import nova.core.util.transform.MatrixStack

class ItemShapeCube extends ItemShape {

	override def getID: String = "modeCube"

	override def getStructure: Structure = new StructureCube

	override def render(projector: Projector, model: Model) {
		model.matrix = new MatrixStack().loadMatrix(model.matrix).scale(0.5, 0.5, 0.5).getMatrix
		BlockModelUtil.drawCube(model)
	}
}
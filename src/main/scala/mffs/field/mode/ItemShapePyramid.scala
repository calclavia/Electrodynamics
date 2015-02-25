package mffs.field.mode

import com.resonant.core.structure.Structure
import mffs.api.machine.Projector
import mffs.field.structure.StructurePyramid
import nova.core.render.model.{Model, Vertex}
import nova.core.util.transform.Vector3d

class ItemShapePyramid extends ItemShape {
	override def getID: String = "modePyramid"

	override def getStructure: Structure = new StructurePyramid

	override def render(projector: Projector, model: Model) {
		val height = 0.5f
		val width = 0.3f
		val uvMaxX = 2
		val uvMaxY = 2
		val translation = new Vector3d(0, -0.4, 0)
		model.rotate(new Vector3d(0, 0, 1), Math.PI)
		val face = model.createFace()
		face.drawVertex(new Vertex(0 + translation.x, 0 + translation.y, 0 + translation.z, 0, 0))
		face.drawVertex(new Vertex(-width + translation.x, height + translation.y, -width + translation.z, -uvMaxX, -uvMaxY))
		face.drawVertex(new Vertex(-width + translation.x, height + translation.y, width + translation.z, -uvMaxX, uvMaxY))
		face.drawVertex(new Vertex(width + translation.x, height + translation.y, width + translation.z, uvMaxX, uvMaxY))
		face.drawVertex(new Vertex(width + translation.x, height + translation.y, -width + translation.z, uvMaxX, -uvMaxY))
		face.drawVertex(new Vertex(-width + translation.x, height + translation.y, -width + translation.z, -uvMaxX, -uvMaxY))
		model.drawFace(face)
	}
}
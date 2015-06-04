package com.calclavia.edx.optics.field.shape

import com.calclavia.edx.optics.api.machine.Projector
import com.calclavia.edx.optics.field.structure.StructureSphere
import com.resonant.core.structure.Structure
import nova.core.render.model.Model
import nova.core.util.transform.vector.Vector3d

class ItemShapeSphere extends ItemShape {

	override def getID: String = "modeSphere"

	override def getStructure: Structure = new StructureSphere

	override def render(projector: Projector, model: Model) {
		val scale = 0.2f
		val radius = 0.8f
		val steps = Math.ceil(Math.PI / Math.atan(1.0D / radius / 2)).toInt
		model.scale(scale, scale, scale)

		for (phi_n <- 0 until 2 * steps; theta_n <- 0 until steps) {
			val phi = Math.PI * 2 / steps * phi_n
			val theta = Math.PI / steps * theta_n
			val vector = new Vector3d(Math.sin(theta) * Math.cos(phi), Math.cos(theta), Math.sin(theta) * Math.sin(phi)) * radius
			val cube = new Model()
			cube.translate(vector)
			model.children.add(cube)
		}
	}
}
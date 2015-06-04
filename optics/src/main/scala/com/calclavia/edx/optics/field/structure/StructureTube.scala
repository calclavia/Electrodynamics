package com.calclavia.edx.optics.field.structure

import com.resonant.core.structure.StructureCube
import nova.core.util.transform.vector.Vector3d

/**
 * @author Calclavia
 */
class StructureTube extends StructureCube {

	/**
	 * Gets the equation that define the 3D surface in standard form.
	 * The transformation should be default.
	 * @return The result of the equation. Zero if the position satisfy the equation.
	 */
	//TODO: Check this equation
	override def surfaceEquation(position: Vector3d): Double = Math.max(Math.abs(position.x), Math.abs(position.z)) - 0.5

	override def getID: String = "tube"
}

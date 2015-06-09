package com.calclavia.edx.optics.field.structure

import com.resonant.core.structure.StructureCube
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

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
	override def surfaceEquation(position: Vector3D): Double = Math.max(Math.abs(position.getX()), Math.abs(position.getZ())) - 0.5

	override def getID: String = "tube"
}

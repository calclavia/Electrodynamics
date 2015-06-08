package com.calclavia.edx.optics.field.structure

import com.resonant.core.structure.Structure
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

/**
 * @author Calclavia
 */
class StructureSphere extends Structure {
	/**
	 * Gets the equation that define the 3D surface in standard form.
	 * The transformation should be default.
	 * @return The result of the equation. Zero if the position satisfy the equation.
	 */
	override def surfaceEquation(position: Vector3D): Double = position.magnitude() - 1

	/**
	 * Gets the equation that define the 3D volume in standard form.
	 * The transformation should be default.
	 * @return The result of the equation. Zero if the position satisfy the equation.
	 */
	override def volumeEquation(position: Vector3D): Double = if (position.magnitude() < 1) 1 else 0

	override def getID: String = "sphere"
}

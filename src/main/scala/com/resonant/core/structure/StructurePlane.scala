package com.resonant.core.structure

import nova.core.block.Block
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

/**
 * Creates a flat plane surface
 */
class StructurePlane(name: String, block: Block) extends Structure {

	/**
	 * Gets the equation that define the 3D volume in standard form.
	 * The transformation should be default.
	 * @return The result of the equation. Zero if the position satisfy the equation.
	 */
	override def volumeEquation(position: Vector3D): Double = surfaceEquation(position)

	/**
	 * Gets the equation that define the 3D surface in standard form.
	 * The transformation should be default.
	 * @return The result of the equation. Zero if the position satisfy the equation.
	 */
	override def surfaceEquation(position: Vector3D): Double = position.getX() + position.getY() + position.getZ()

	override def getID: String = "plane"
}
package com.resonant.core.structure

import nova.core.util.shape.Cuboid
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

/**
 * A cube structure.
 * @author Calclavia
 */
class StructureCube extends Structure {

	/**
	 * Gets the equation that define the 3D structure's surface.
	 */
	override def surfaceEquation(position: Vector3D): Double = Math.max(Math.abs(position.getX()), Math.max(Math.abs(position.getY()), Math.abs(position.getZ()))) - 0.5

	/**
	 * Gets the equation that define the 3D volume in standard form.
	 * The transformation should be default.
	 * @return The result of the equation. Zero if the position satisfy the equation.
	 */
	override def volumeEquation(position: Vector3D): Double = if (new Cuboid(new Vector3D(-0.5, -0.5, -0.5), new Vector3D(0.5, 0.5, 0.5)).intersects(position)) 0 else 1

	override def getID: String = "Cube"
}

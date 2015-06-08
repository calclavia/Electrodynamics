package com.calclavia.edx.optics.field.structure

import com.resonant.core.structure.Structure
import nova.core.util.transform.shape.Cuboid
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

/**
 * @author Calclavia
 */
class StructurePyramid extends Structure {

	val xSize = 1
	val ySize = 1
	val zSize = 1

	val xDecr = xSize / ySize
	val zDecr = zSize / ySize

	/**
	 * Gets the equation that define the 3D surface in standard form.
	 * The transformation should be default.
	 * @return The result of the equation. Zero if the position satisfy the equation.
	 */
	override def surfaceEquation(position: Vector3D): Double = {

		val yEquation = position.getY() - ySize / 2
		val xEquation = position.getX() - (xSize - xDecr * (position.getY() + 0.5))
		val zEquation = position.getZ() - (zSize - zDecr * (position.getY() + 0.5))
		return Math.max(Math.abs(yEquation), Math.max(Math.abs(xEquation), Math.abs(zEquation)))
		/*
		for (y <- -ySize to ySize) {
			for (x <- -initX to initX; z <- -initZ to initZ) {
				if (Math.abs(x) == Math.round(xSize) && Math.abs(z) <= Math.round(zSize)) {
					fieldBlocks.add(new Vector3d(x, y, z))
				}
				else if (Math.abs(z) == Math.round(zSize) && Math.abs(x) <= Math.round(xSize)) {
					fieldBlocks.add(new Vector3d(x, y, z))
				}
				else if (y == -ySize) {
					fieldBlocks.add(new Vector3d(x, y, z))
				}
			}

			xSize -= xDecr
			zSize -= zDecr
		}*/
	}

	/**
	 * Gets the equation that define the 3D volume in standard form.
	 * The transformation should be default.
	 * @return The result of the equation. Zero if the position satisfy the equation.
	 */
	override def volumeEquation(position: Vector3D): Double = {
		val region: Cuboid = new Cuboid(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5)

		if (region.intersects(position) && position.getY() > ySize / 2) {
			if (1 - (Math.abs(position.getX()) / xSize) - (Math.abs(position.getZ()) / zSize) > position.getY() / ySize) {
				return 1
			}
		}

		return 0
	}

	override def getID: String = "pyramid"
}

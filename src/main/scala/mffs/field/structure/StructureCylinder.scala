package mffs.field.structure

import com.resonant.core.structure.Structure
import nova.core.util.transform.Vector3d

/**
 * @author Calclavia
 */
class StructureCylinder extends Structure {

	private val radiusExpansion = 0
	private val height = 2
	private val radius = 1

	/**
	 * Gets the equation that define the 3D surface in standard form.
	 * The transformation should be default.
	 * @return The result of the equation. Zero if the position satisfy the equation.
	 */
	override def surfaceEquation(position: Vector3d): Double = {
		if ((position.y == 0 || position.y == height - 1) && (position.x * position.x + position.z * position.z + radiusExpansion) <= (radius * radius)) {
			return 1
		}

		if ((position.x * position.x + position.z * position.z + radiusExpansion) <= (radius * radius) && (position.x * position.x + position.z * position.z + radiusExpansion) >= ((radius - 1) * (radius - 1))) {
			return 1
		}
		return 0
	}

	/**
	 * Gets the equation that define the 3D volume in standard form.
	 * The transformation should be default.
	 * @return The result of the equation. Zero if the position satisfy the equation.
	 */
	override def volumeEquation(position: Vector3d): Double = {
		if (position.x * position.x + position.z * position.z + radiusExpansion <= radius * radius) {
			return 1
		}

		return 0
	}

	override def getID: String = "cylinder"
}

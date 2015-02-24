package mffs.field.structure

import com.resonant.core.structure.Structure
import nova.core.util.transform.Vector3d

/**
 * @author Calclavia
 */
class StructureSphere extends Structure {
	/**
	 * Gets the equation that define the 3D surface in standard form.
	 * The transformation should be default.
	 * @return The result of the equation. Zero if the position satisfy the equation.
	 */
	override def surfaceEquation(position: Vector3d): Double = position.magnitude() - 1

	/**
	 * Gets the equation that define the 3D volume in standard form.
	 * The transformation should be default.
	 * @return The result of the equation. Zero if the position satisfy the equation.
	 */
	override def volumeEquation(position: Vector3d): Double = if (position.magnitude() < 1) 1 else 0

	override def getID: String = "sphere"
}

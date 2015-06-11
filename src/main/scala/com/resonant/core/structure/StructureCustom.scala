package com.resonant.core.structure

import java.util

import nova.core.block.BlockFactory
import nova.core.retention.{Data, Storable}
import nova.core.util.math.{MatrixStack, TransformUtil}
import nova.internal.core.Game
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import scala.collection.convert.wrapAll._

/**
 * Custom structure based on stored state.
 * @author Calclavia
 */
class StructureCustom(val name: String) extends Structure with Storable {

	/**
	 * A map of unit vector to block positions.
	 */
	var structure = Map.empty[Vector3D, String]

	override def getExteriorStructure: Set[Vector3D] = {
		return getBlockStructure.keySet
	}

	override def getBlockStructure: Map[Vector3D, BlockFactory] = {
		val matrix = new MatrixStack().translate(translate).scale(scale).rotate(rotation).getMatrix
		return structure
			.filter(kv => Game.blocks.get(kv._2).isPresent)
			.map(e => (TransformUtil.transform(e._1, matrix), Game.blocks.getFactory(e._2).get()))
	}

	override def load(data: Data) {
		structure = data.get("structure").asInstanceOf[util.Map[Vector3D, String]].toMap
	}

	override def save(data: Data) {
		data.put("structure", structure.asInstanceOf[util.Map[Vector3D, String]])
	}

	/**
	 * Gets the equation that define the 3D surface in standard form.
	 * The transformation should be default.
	 * @return The result of the equation. Zero if the position satisfy the equation.
	 */
	override def surfaceEquation(position: Vector3D): Double = Double.PositiveInfinity

	/**
	 * Gets the equation that define the 3D volume in standard form.
	 * The transformation should be default.
	 * @return The result of the equation. Zero if the position satisfy the equation.
	 */
	override def volumeEquation(position: Vector3D): Double = Double.PositiveInfinity

	override def getID: String = name
}

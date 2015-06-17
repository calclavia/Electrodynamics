package com.resonant.core.structure

import java.util.Optional

import com.google.common.math.DoubleMath
import nova.core.block.BlockFactory
import nova.core.util.Identifiable
import nova.core.util.math.{MatrixStack, TransformUtil, Vector3DUtil}
import nova.scala.wrapper.VectorWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.{Rotation, Vector3D}
import org.apache.commons.math3.linear.LUDecomposition

import scala.beans.BeanProperty
import scala.collection.parallel
import scala.collection.parallel.ParSet

/**
 * Defines a 3D structure.
 * @author Calclavia
 */
abstract class Structure extends Identifiable {

	//The error allowed in fuzzy comparisons
	@BeanProperty
	var error = 0.001
	@BeanProperty
	var stepSize = 1.0
	@BeanProperty
	var translate = Vector3D.ZERO
	@BeanProperty
	var scale = Vector3DUtil.ONE
	@BeanProperty
	var rotation = Rotation.IDENTITY
	@BeanProperty
	var blockFactory = Optional.empty[BlockFactory]()
	/**
	 * A mapper that acts as a custom transformation function
	 */
	var preMapper: PartialFunction[Vector3D, Vector3D] = {
		case pos: Vector3D => pos
	}

	var postMapper: PartialFunction[Vector3D, Vector3D] = {
		case pos: Vector3D => pos
	}

	var postStructure = (positions: Set[Vector3D]) => positions

	/**
	 * Do a search within an appropriate region by generating a search set.
	 */
	def searchSpace: parallel.ParIterable[Vector3D] = {
		var search = ParSet.empty[Vector3D]

		for (x <- -scale.x / 2 to scale.x / 2 by stepSize; y <- -scale.y / 2 to scale.y / 2 by stepSize; z <- -scale.z / 2 to scale.z / 2 by stepSize) {
			search += new Vector3D(x, y, z)
		}
		return search
	}

	def getExteriorStructure: Set[Vector3D] = getStructure(surfaceEquation)

	def getInteriorStructure: Set[Vector3D] = getStructure(volumeEquation)

	def getBlockStructure: Map[Vector3D, BlockFactory] = {
		//TODO: Should be exterior?
		return getExteriorStructure
			.filter(getBlockFactory(_).isPresent)
			.map(v => (v, getBlockFactory(v).get()))
			.toMap
	}

	protected def getStructure(equation: (Vector3D) => Double): Set[Vector3D] = {
		if (scale.getNorm > 0) {
			val matrix = new MatrixStack().rotate(rotation).scale(scale).getMatrix
			val transformMatrix = new LUDecomposition(matrix).getSolver.getInverse

			/**
			 * The equation has default transformations.
			 * Therefore, we need to transform the test vector back into the default, to test against the equation
			 */
			val structure = searchSpace
				.collect(preMapper)
				.filter(v => DoubleMath.fuzzyEquals(equation(TransformUtil.transform(v, transformMatrix)), 0, error))
				.map(_ + translate)
				.map(_.floor)
				.collect(postMapper)
				.seq
				.toSet

			return postStructure(structure)
		}
		return Set.empty
	}

	/**
	 * Gets the block at this position (relatively) 
	 * @param position
	 * @return
	 */
	def getBlockFactory(position: Vector3D): Optional[BlockFactory] = blockFactory

	/**
	 * Checks if this world position is within this structure. 
	 * @param position The world position
	 * @return True if there is an intersection
	 */
	def intersects(position: Vector3D): Boolean = {
		//TODO: Use negate matrix
		val rotationMatrix = new MatrixStack().rotate(rotation).getMatrix
		return DoubleMath.fuzzyEquals(volumeEquation(TransformUtil.transform(position - translate, rotationMatrix) / scale), 0, error)
	}

	/**
	 * Gets the equation that define the 3D surface in standard form.
	 * The transformation should be default.
	 * @return The result of the equation. Zero if the position satisfy the equation.
	 */
	def surfaceEquation(position: Vector3D): Double

	/**
	 * Gets the equation that define the 3D volume in standard form.
	 * The transformation should be default.
	 * @return The result of the equation. Zero if the position satisfy the equation.
	 */
	def volumeEquation(position: Vector3D): Double
}
package com.calclavia.edx.electric.grid

import java.util.Optional

import com.resonant.lib.WrapFunctions._
import nova.core.block.Block
import nova.core.block.component.Connectable
import nova.core.util.Direction
import nova.core.util.transform.vector.Vector3i
import nova.core.world.World

import scala.collection.convert.wrapAll._

/**
 * A node that connects to adjacent blocks.
 * @author Calclavia
 */
@deprecated
trait BlockConnectable[N] extends Connectable[N] {

	/**
	 * @return The block assosiated with this node.
	 */
	protected def block: Block

	var connectedMask = 0x3F

	connections = supplier(() => {
		val adjacentBlocks: Map[Direction, Optional[Block]] = this.adjacentBlocks
		val adjacentNodes: Map[Direction, N] =
			adjacentBlocks
				.filter { case (k, v) => v.isPresent && v.get.getClass().isAssignableFrom(compareClass) }
				.map { case (k, v) => (k, getNodeFromBlock(v.get(), k)) }

		val connectedMap = adjacentNodes
			.filter { case (k, v) => canConnect(v) }
			.filter { case (k, v) => v.asInstanceOf[Connectable[N]].canConnect(this.asInstanceOf[N]) }
			.map(_.swap)

		connectedMask = connectedMap.values
			.map(_.ordinal)
			.map(i => 1 << i)
			.foldLeft(0)((a, b) => a | b)

		connectedMap.keySet
	})

	def setConnections(f: () => Set[N]) {
		connections = supplier(() => f.get())
	}

	/**
	 * @return The set of blocks adjacent to this block
	 */
	protected def adjacentBlocks: Map[Direction, Optional[Block]] = Direction.DIRECTIONS.map(dir => (dir, world.getBlock(position + dir.toVector))).toMap

	protected def getNodeFromBlock(block: Block, from: Direction): N = block.getOp(compareClass).orElse(null.asInstanceOf[N])

	def world: World = block.world()

	def position: Vector3i = block.transform.position

	protected def compareClass: Class[N] = getClass.asInstanceOf[Class[N]]
}

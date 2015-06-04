package com.calclavia.edx.optics.field.mobilize

import nova.core.block.Block
import nova.core.block.Block.DropEvent
import nova.core.game.Game
import nova.core.util.transform.vector.Vector3i
import nova.core.world.World

import scala.collection.convert.wrapAll._

@deprecated
class BlockDropDelayedEvent(ticks: Int, block: Block, world: World, position: Vector3i) extends DelayedEvent(ticks) {
	protected override def onEvent {
		if (Game.network.isServer) {
			val checkBlock = world.getBlock(position)

			if (checkBlock.isPresent && checkBlock.get == block) {
				val dropEvt = new DropEvent(block)
				val drops = block.dropEvent.publish(dropEvt)
				dropEvt.drops.foreach(drop => world.addEntity(position.toDouble + 0.5, drop))
				world.removeBlock(position)
			}
		}
	}
}
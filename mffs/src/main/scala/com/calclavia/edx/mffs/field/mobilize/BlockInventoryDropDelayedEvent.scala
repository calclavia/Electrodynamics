package com.calclavia.edx.mffs.field.mobilize

import nova.core.block.Block
import nova.core.game.Game
import nova.core.inventory.component.InventoryProvider
import nova.core.util.transform.vector.Vector3i
import nova.core.world.World

import scala.collection.convert.wrapAll._

@deprecated
class BlockInventoryDropDelayedEvent(ticks: Int, block: Block, world: World, position: Vector3i, inv: InventoryProvider) extends BlockDropDelayedEvent(ticks, block, world, position) {
	protected override def onEvent {
		if (Game.network.isServer) {
			val checkBlock = world.getBlock(position)

			if (checkBlock.isPresent && checkBlock.get == block) {
				val drops = block.getDrops()
				drops.foreach(inv.getInventory().head.add)
				world.removeBlock(position)
			}
		}
	}
}
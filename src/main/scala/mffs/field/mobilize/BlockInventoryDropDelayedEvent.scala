package mffs.field.mobilize

import nova.core.block.Block
import nova.core.game.Game
import nova.core.inventory.components.InventoryProvider
import nova.core.util.Direction
import nova.core.util.transform.Vector3i
import nova.core.world.World

import scala.collection.convert.wrapAll._

@deprecated
class BlockInventoryDropDelayedEvent(ticks: Int, block: Block, world: World, position: Vector3i, inv: InventoryProvider) extends BlockDropDelayedEvent(ticks, block, world, position) {
	protected override def onEvent {
		if (Game.instance.networkManager.isServer) {
			val checkBlock = world.getBlock(position)

			if (checkBlock.isPresent && checkBlock.get == block) {
				val drops = block.getDrops()
				drops.foreach(inv.getInventory(Direction.UNKNOWN).get().add)
				world.setBlock(position, Game.instance.blockManager.getAirBlock)
			}
		}
	}
}
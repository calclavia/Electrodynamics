package com.calclavia.edx.optics.field.mobilize

import com.calclavia.edx.core.EDX
import nova.core.block.Block
import nova.core.block.Block.DropEvent
import nova.core.component.ComponentProvider
import nova.core.inventory.Inventory
import nova.core.world.World
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import scala.collection.convert.wrapAll._

@deprecated
class BlockInventoryDropDelayedEvent(ticks: Int, block: Block, world: World, position: Vector3D, inv: ComponentProvider) extends BlockDropDelayedEvent(ticks, block, world, position) {
	protected override def onEvent {
		if (EDX.network.isServer) {
			val checkBlock = world.getBlock(position)

			if (checkBlock.isPresent && checkBlock.get == block) {
				val evt = new DropEvent(block)
				val drops = block.events.publish(evt)
				evt.drops.foreach(inv.get(classOf[Inventory]).add)
				world.removeBlock(position)
			}
		}
	}
}
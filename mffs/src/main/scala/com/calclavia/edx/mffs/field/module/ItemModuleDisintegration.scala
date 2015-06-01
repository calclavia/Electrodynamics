package com.calclavia.edx.mffs.field.module

import java.util

import com.calclavia.edx.mffs.api.Blacklist
import com.calclavia.edx.mffs.api.machine.Projector
import com.calclavia.edx.mffs.api.modules.Module.ProjectState
import com.calclavia.edx.mffs.base.{ItemModule, PacketBlock}
import com.calclavia.edx.mffs.content.Content
import com.calclavia.edx.mffs.field.BlockProjector
import com.calclavia.edx.mffs.field.mobilize.{BlockDropDelayedEvent, BlockInventoryDropDelayedEvent}
import com.calclavia.edx.mffs.util.MFFSUtility
import nova.core.game.Game
import nova.core.inventory.component.InventoryProvider
import nova.core.util.transform.vector.Vector3i

class ItemModuleDisintegration extends ItemModule {
	private var blockCount = 0
	setMaxCount(1)
	setCost(20)

	override def getID: String = "moduleDisintegration"

	override def onCreateField(projector: Projector, field: util.Set[Vector3i]): Boolean = {
		blockCount = 0
		return false
	}

	override def onProject(projector: Projector, position: Vector3i): ProjectState = {
		val proj = projector.asInstanceOf[BlockProjector]
		val world = proj.world

		val opBlock = world.getBlock(position)

		if (opBlock.isPresent) {
			val block = opBlock.get()

			//TODO: Check this
			val filterMatch = !proj.getFilterItems.exists(item => MFFSUtility.getFilterBlock(item) != null && item.sameItemType(item))

			if (proj.isInvertedFilter != filterMatch) {
				return ProjectState.pass
			}

			//|| block.isInstanceOf[BlockLiquid] || block.isInstanceOf[IFluidBlock]
			if (Blacklist.disintegrationBlacklist.contains(block)) {
				return ProjectState.pass
			}

			Game.networkManager.sync(PacketBlock.effect, proj)

			if (projector.getModuleCount(Content.moduleCollection) > 0) {
				Game.syncTicker.preQueue(new BlockInventoryDropDelayedEvent(39, block, world, position, projector.asInstanceOf[InventoryProvider]))
			}
			else {
				Game.syncTicker.preQueue(new BlockDropDelayedEvent(39, block, world, position))
			}

			blockCount += 1

			if (blockCount >= projector.getModuleCount(Content.moduleSpeed) / 3) {
				return ProjectState.cancel
			}
			else {
				return ProjectState.pass
			}
		}

		return ProjectState.pass
	}

	override def getFortronCost(amplifier: Float): Float = super.getFortronCost(amplifier) + (super.getFortronCost(amplifier) * amplifier)

}
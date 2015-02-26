package mffs.field.module

import java.util

import mffs.api.Blacklist
import mffs.api.machine.Projector
import mffs.api.modules.Module.ProjectState
import mffs.base.{BlockInventory, ItemModule, PacketBlock}
import mffs.content.Content
import mffs.field.BlockProjector
import mffs.field.mobilize.event.{BlockDropDelayedEvent, BlockInventoryDropDelayedEvent}
import mffs.util.MFFSUtility
import nova.core.game.Game
import nova.core.util.transform.Vector3i

class ItemModuleDisintegration extends ItemModule {
	private var blockCount = 0
	setMaxCount(1)
	setCost(20)

	override def getID: String = "moduleDisintegration"

	override def onCreateField(projector: Projector, field: util.Set[Vector3i]): Boolean = {
		blockCount = 0
		super.onCreateField(projector, field)
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

			Game.instance.networkManager.sync(PacketBlock.effect.ordinal(), proj)

			if (projector.getModuleCount(Content.moduleCollection) > 0) {
				Game.instance.syncTicker.preQueue(new BlockInventoryDropDelayedEvent(39, block, world, position, projector.asInstanceOf[BlockInventory]))
			}
			else {
				Game.instance.syncTicker.preQueue(new BlockDropDelayedEvent(39, block, world, position))
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
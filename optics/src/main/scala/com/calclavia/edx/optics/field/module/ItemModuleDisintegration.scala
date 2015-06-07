package com.calclavia.edx.optics.field.module

import java.util

import com.calclavia.edx.optics.api.Blacklist
import com.calclavia.edx.optics.api.machine.Projector
import com.calclavia.edx.optics.api.modules.Module.ProjectState
import com.calclavia.edx.optics.base.{ItemModule, PacketBlock}
import com.calclavia.edx.optics.content.OpticsContent
import com.calclavia.edx.optics.field.BlockProjector
import com.calclavia.edx.optics.field.mobilize.{BlockDropDelayedEvent, BlockInventoryDropDelayedEvent}
import com.calclavia.edx.optics.util.MFFSUtility
import nova.core.component.ComponentProvider
import com.calclavia.edx.core.EDX
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

			EDX.network.sync(PacketBlock.effect, proj)

			if (projector.getModuleCount(OpticsContent.moduleCollection) > 0) {
				EDX.syncTicker.preQueue(new BlockInventoryDropDelayedEvent(39, block, world, position, projector.asInstanceOf[ComponentProvider]))
			}
			else {
				EDX.syncTicker.preQueue(new BlockDropDelayedEvent(39, block, world, position))
			}

			blockCount += 1

			if (blockCount >= projector.getModuleCount(OpticsContent.moduleSpeed) / 3) {
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
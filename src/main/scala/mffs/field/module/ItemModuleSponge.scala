package mffs.field.module

import java.util

import mffs.api.machine.Projector
import mffs.base.ItemModule
import mffs.field.BlockProjector
import nova.core.fluid.FluidBlock
import nova.core.game.Game
import nova.core.util.transform.Vector3i

class ItemModuleSponge extends ItemModule {
	setMaxCount(1)

	override def getID: String = "moduleSponge"

	override def onCreateField(projector: Projector, field: util.Set[Vector3i]): Boolean = {
		val proj = projector.asInstanceOf[BlockProjector]

		if (projector.getTicks % 60 == 0) {
			val world = proj.world

			if (Game.instance.networkManager.isServer) {
				for (point <- projector.getInteriorPoints) {
					val block = world.getBlock(point)

					if (block.isInstanceOf[FluidBlock]) {
						world.setBlock(point, Game.instance.blockManager.getAirBlock)
					}
				}
			}
		}

		return super.onCreateField(projector, field)
	}

	override def requireTicks = true
}
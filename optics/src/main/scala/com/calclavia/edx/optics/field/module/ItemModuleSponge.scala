package com.calclavia.edx.optics.field.module

import java.util

import com.calclavia.edx.optics.api.machine.Projector
import com.calclavia.edx.optics.base.ItemModule
import com.calclavia.edx.optics.field.BlockProjector
import nova.core.fluid.FluidBlock
import nova.core.network.NetworkTarget.Side
import nova.core.util.transform.vector.Vector3i

import scala.collection.convert.wrapAll._

class ItemModuleSponge extends ItemModule {
	setMaxCount(1)

	override def getID: String = "moduleSponge"

	override def onCreateField(projector: Projector, field: util.Set[Vector3i]): Boolean = {
		val proj = projector.asInstanceOf[BlockProjector]

		if (proj.getTicks % 60 == 0) {
			val world = proj.world

			if (Side.get().isServer) {
				for (point <- projector.getInteriorPoints) {
					val block = world.getBlock(point).get

					if (block.isInstanceOf[FluidBlock]) {
						world.removeBlock(point)
					}
				}
			}
		}

		return false
	}

	override def requireTicks = true
}
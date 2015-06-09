package com.calclavia.edx.optics.item.card

import com.calclavia.edx.optics.api.card.CoordLink
import nova.scala.wrapper.FunctionalWrapper
import FunctionalWrapper._
import com.calclavia.edx.core.EDX
import nova.core.item.Item.{TooltipEvent, UseEvent}
import nova.core.retention.{Storable, Store}
import nova.core.util.collection.Tuple2
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import nova.core.world.World

/**
 * A linking card used to link machines in specific positions.
 *
 * @author Calclavia
 */
class ItemCardLink extends ItemCard with CoordLink with Storable {

	@Store
	var linkWorld: World = null
	@Store
	var linkPos: Vector3D = null

	override def setLink(world: World, position: Vector3D) {
		linkWorld = world
		linkPos = position
	}

	override def getLink: Tuple2[World, Vector3D] = new Tuple2(linkWorld, linkPos)

	tooltipEvent.add(eventListener((evt: TooltipEvent) => {
		if (linkWorld != null && linkPos != null) {
			val block = linkWorld.getBlock(linkPos)

			if (block.isPresent) {
				evt.tooltips.add(EDX.language.translate("info.item.linkedWith") + " " + block.get().getID)
			}

			evt.tooltips.add(linkPos.getX() + ", " + linkPos.getY() + ", " + linkPos.getZ())
			evt.tooltips.add(EDX.language.translate("info.item.dimension") + " " + linkWorld.getID)
		}
		else {
			evt.tooltips.add(EDX.language.translate("info.item.notLinked"))
		}
	}))

	useEvent.add((evt: UseEvent) => {
		if (EDX.network.isServer) {

			val block = evt.entity.world.getBlock(evt.position)
			if (block.isPresent) {
				linkWorld = evt.entity.world
				linkPos = evt.position
				//TODO: Fix chat msg
				//player.addChatMessage(new ChatComponentTranslation("info.item.linkedWith", x + ", " + y + ", " + z + " - " + vector.getBlock(world).getLocalizedName))
			}
		}
		evt.action = true
	})

	override def getID: String = "cardLink"
}
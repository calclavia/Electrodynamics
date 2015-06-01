package com.calclavia.edx.mffs.item.card

import com.calclavia.edx.mffs.api.Frequency
import com.google.common.hash.Hashing
import com.resonant.lib.WrapFunctions
import WrapFunctions._
import nova.core.game.Game
import nova.core.item.Item.{RightClickEvent, TooltipEvent}
import nova.core.retention.Stored
import nova.core.util.transform.vector.Vector3i

import scala.beans.BeanProperty

class ItemCardFrequency extends ItemCard with Frequency {

	@Stored
	@BeanProperty
	var frequency: Int = 0

	tooltipEvent.add(eventListener((evt: TooltipEvent) => evt.tooltips.add(Game.language.translate("info.cardFrequency.freq") + " " + getEncodedFrequency)))

	def getEncodedFrequency = Hashing.md5().hashInt(frequency).toString.take(12)

	rightClickEvent.add((evt: RightClickEvent) => {
		if (Game.network.isServer) {
			Game.gui.showGui("cardFrequency", evt.entity, Vector3i.zero)
		}
	})

	override def getID: String = "cardFrequency"
}
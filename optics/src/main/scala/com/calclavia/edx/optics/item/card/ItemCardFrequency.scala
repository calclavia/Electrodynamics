package com.calclavia.edx.optics.item.card

import com.calclavia.edx.optics.api.Frequency
import com.google.common.hash.Hashing
import com.resonant.lib.WrapFunctions._
import com.calclavia.edx.core.EDX
import nova.core.item.Item.{RightClickEvent, TooltipEvent}
import nova.core.retention.Store
import nova.core.util.transform.vector.Vector3i

import scala.beans.BeanProperty

class ItemCardFrequency extends ItemCard with Frequency {

	@Store
	@BeanProperty
	var frequency: Int = 0

	tooltipEvent.add(eventListener((evt: TooltipEvent) => evt.tooltips.add(EDX.language.translate("info.cardFrequency.freq") + " " + getEncodedFrequency)))

	def getEncodedFrequency = Hashing.md5().hashInt(frequency).toString.take(12)

	/*
		rightClickEvent.add((evt: RightClickEvent) => {
			if (EDX.network.isServer) {
				EDX.gui.showGui("cardFrequency", evt.entity, Vector3i.zero)
			}
		})
	*/
	override def getID: String = "cardFrequency"
}
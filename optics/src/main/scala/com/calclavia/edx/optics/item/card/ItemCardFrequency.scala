package com.calclavia.edx.optics.item.card

import com.calclavia.edx.core.EDX
import com.calclavia.edx.optics.api.Frequency
import com.google.common.hash.Hashing
import nova.core.item.Item.TooltipEvent
import nova.core.retention.Store
import nova.scala.wrapper.FunctionalWrapper._

import scala.beans.BeanProperty

class ItemCardFrequency extends ItemCard with Frequency {

	@Store
	@BeanProperty
	var frequency: Int = 0

	events.on(classOf[TooltipEvent]).bind(eventListener((evt: TooltipEvent) => evt.tooltips.add(EDX.language.translate("info.cardFrequency.freq") + " " + getEncodedFrequency)))

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
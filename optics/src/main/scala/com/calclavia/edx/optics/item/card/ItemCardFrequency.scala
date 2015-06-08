package com.calclavia.edx.optics.item.card

import com.calclavia.edx.optics.api.Frequency
import com.google.common.hash.Hashing
import nova.scala.wrapper.FunctionalWrapper
import FunctionalWrapper._
import com.calclavia.edx.core.EDX
import nova.core.item.Item.{RightClickEvent, TooltipEvent}
import nova.core.retention.Store
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

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
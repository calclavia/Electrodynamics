package com.calclavia.edx.optics.security.module

import com.calclavia.edx.optics.base.ItemModule
import nova.scala.wrapper.FunctionalWrapper
import FunctionalWrapper._
import com.calclavia.edx.core.EDX
import nova.core.item.Item.TooltipEvent

class ItemModuleDefense extends ItemModule {
	tooltipEvent.add(eventListener((evt: TooltipEvent) => evt.tooltips.add("\u00a74" + EDX.language.translate("info.module.defense"))))

	override def getID: String = "moduleDefense"
}
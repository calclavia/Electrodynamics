package com.calclavia.edx.optics.security.module

import com.calclavia.edx.core.EDX
import com.calclavia.edx.optics.component.ItemModule
import nova.core.item.Item.TooltipEvent
import nova.scala.wrapper.FunctionalWrapper._

class ItemModuleDefense extends ItemModule {
	events.on(classOf[TooltipEvent]).bind(eventListener((evt: TooltipEvent) => evt.tooltips.add("\u00a74" + EDX.language.translate("info.module.defense"))))

	override def getID: String = "moduleDefense"
}
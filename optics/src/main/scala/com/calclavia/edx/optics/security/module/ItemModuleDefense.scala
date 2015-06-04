package com.calclavia.edx.optics.security.module

import com.calclavia.edx.optics.base.ItemModule
import com.resonant.lib.WrapFunctions._
import nova.core.game.Game
import nova.core.item.Item.TooltipEvent

class ItemModuleDefense extends ItemModule {
	tooltipEvent.add(eventListener((evt: TooltipEvent) => evt.tooltips.add("\u00a74" + Game.language.translate("info.module.defense"))))

	override def getID: String = "moduleDefense"
}
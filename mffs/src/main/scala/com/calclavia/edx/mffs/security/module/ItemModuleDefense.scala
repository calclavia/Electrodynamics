package com.calclavia.edx.mffs.security.module

import com.calclavia.edx.mffs.base.ItemModule
import com.resonant.lib.wrapper.WrapFunctions._
import nova.core.game.Game
import nova.core.item.Item.TooltipEvent

class ItemModuleDefense extends ItemModule {
	tooltipEvent.add(eventListener((evt: TooltipEvent) => evt.tooltips.add("\u00a74" + Game.instance.languageManager.translate("info.module.defense"))))

	override def getID: String = "moduleDefense"
}
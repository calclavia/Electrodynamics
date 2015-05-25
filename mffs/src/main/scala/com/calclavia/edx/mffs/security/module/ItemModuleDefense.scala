package com.calclavia.edx.mffs.security.module

import java.util
import java.util.Optional

import com.calclavia.edx.mffs.base.ItemModule
import nova.core.entity.component.Player
import nova.core.game.Game

class ItemModuleDefense extends ItemModule {
	override def getTooltips(player: Optional[Player], tooltips: util.List[String]) {
		super.getTooltips(player, tooltips)
		tooltips.add("\u00a74" + Game.instance.languageManager.translate("info.module.defense"))
	}

	override def getID: String = "moduleDefense"
}
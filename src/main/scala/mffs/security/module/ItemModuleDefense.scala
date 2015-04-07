package mffs.security.module

import java.util
import java.util.Optional

import mffs.base.ItemModule
import nova.core.game.Game
import nova.core.player.Player

class ItemModuleDefense extends ItemModule {
	override def getTooltips(player: Optional[Player], tooltips: util.List[String]) {
		super.getTooltips(player, tooltips)
		tooltips.add("\u00a74" + Game.instance.languageManager.getLocal("info.module.defense"))
	}

	override def getID: String = "moduleDefense"
}
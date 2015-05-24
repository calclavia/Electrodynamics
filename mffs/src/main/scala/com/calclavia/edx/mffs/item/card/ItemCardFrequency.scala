package com.calclavia.edx.mffs.item.card

import java.util
import java.util.Optional

import com.calclavia.edx.mffs.api.Frequency
import com.google.common.hash.Hashing
import nova.core.entity.Entity
import nova.core.game.Game
import nova.core.player.Player
import nova.core.retention.Stored
import nova.core.util.transform.vector.Vector3i

import scala.beans.BeanProperty

class ItemCardFrequency extends ItemCard with Frequency {

	@Stored
	@BeanProperty
	var frequency: Int = 0

	override def getTooltips(player: Optional[Player], tooltips: util.List[String]) {
		super.getTooltips(player, tooltips)
		tooltips.add(Game.instance.languageManager.translate("info.cardFrequency.freq") + " " + getEncodedFrequency)
	}

	def getEncodedFrequency = Hashing.md5().hashInt(frequency).toString.take(12)

	override def onRightClick(entity: Entity) {
		if (Game.instance.networkManager.isServer) {
			Game.instance.guiFactory.showGui("cardFrequency", entity, Vector3i.zero)
		}
	}

	override def getID: String = "cardFrequency"
}
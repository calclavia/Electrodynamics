package mffs.item.card

import java.util
import java.util.Optional

import com.google.common.hash.Hashing
import mffs.Reference
import mffs.api.Frequency
import nova.core.entity.Entity
import nova.core.game.Game
import nova.core.item.Item
import nova.core.player.Player
import nova.core.retention.Stored

import scala.beans.BeanProperty

class ItemCardFrequency extends ItemCard with Frequency {

	@Stored
	@BeanProperty
	val frequency: Int = 0

	override def getTooltips(player: Optional[Player], tooltips: util.List[String]) {
		super.getTooltips(player, tooltips)
		tooltips.add(Game.instance.languageManager.getLocal("info.cardFrequency.freq") + " " + getEncodedFrequency(Item))
	}

	def getEncodedFrequency = Hashing.md5().hashInt(frequency).toString.take(12)

	override def onRightClick(entity: Entity) {
		if (Game.instance.networkManager.isServer) {
			Game.instance.guiFactory.get().showGui(Reference.id, "cardFrequency", entity)
		}
	}

}
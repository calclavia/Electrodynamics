package mffs.item.card

import java.util
import java.util.Optional

import mffs.api.card.CoordLink
import nova.core.entity.Entity
import nova.core.game.Game
import nova.core.player.Player
import nova.core.retention.{Storable, Stored}
import nova.core.util.Direction
import nova.core.util.collection.Pair
import nova.core.util.transform.vector.{Vector3d, Vector3i}
import nova.core.world.World

/**
 * A linking card used to link machines in specific positions.
 *
 * @author Calclavia
 */
class ItemCardLink extends ItemCard with CoordLink with Storable {

	@Stored
	var linkWorld: World = null
	@Stored
	var linkPos: Vector3i = null

	override def setLink(world: World, position: Vector3i) {
		linkWorld = world
		linkPos = position
	}

	override def getLink: Pair[World, Vector3i] = new Pair(linkWorld, linkPos)

	override def getTooltips(player: Optional[Player], tooltips: util.List[String]) {
		super.getTooltips(player, tooltips)

		if (linkWorld != null && linkPos != null) {
			val block = linkWorld.getBlock(linkPos)

			if (block.isPresent) {
				tooltips.add(Game.instance.languageManager.translate("info.item.linkedWith") + " " + block.get().getID)
			}

			tooltips.add(linkPos.x + ", " + linkPos.y + ", " + linkPos.z)
			tooltips.add(Game.instance.languageManager.translate("info.item.dimension") + " " + linkWorld.getID)
		}
		else {
			tooltips.add(Game.instance.languageManager.translate("info.item.notLinked"))
		}
	}

	override def onUse(entity: Entity, world: World, position: Vector3i, side: Direction, hit: Vector3d): Boolean = {
		super.onUse(entity, world, position, side, hit)

		if (Game.instance.networkManager.isServer) {

			val block = world.getBlock(position)
			if (block.isPresent) {
				linkWorld = world
				linkPos = position
				//TODO: Fix chat msg
				//player.addChatMessage(new ChatComponentTranslation("info.item.linkedWith", x + ", " + y + ", " + z + " - " + vector.getBlock(world).getLocalizedName))
			}
		}
		return true
	}

	override def getID: String = "cardLink"
}
package mffs.security.card

import java.util
import java.util.Optional

import com.resonant.core.access.{AccessUser, Permissions}
import mffs.ModularForceFieldSystem
import nova.core.entity.Entity
import nova.core.game.Game
import nova.core.gui.KeyManager.Key
import nova.core.item.Item
import nova.core.network.NetworkTarget.Side
import nova.core.network.{Packet, PacketHandler}
import nova.core.player.Player

class ItemCardIdentification extends ItemCardAccess with PacketHandler {

	override def hitEntity(Item: Item, entityLiving: EntityLivingBase, par3EntityLiving: EntityLivingBase): Boolean = {
		if (entityLiving.isInstanceOf[Player]) {
			access = new AccessUser(entityLiving.asInstanceOf[Player].getDisplayName)
		}

		return false
	}

	override def getTooltips(player: Optional[Player], tooltips: util.List[String]) {
		super.getTooltips(player, tooltips)

		if (access != null) {
			tooltips.add(Game.instance.languageManager.getLocal("info.cardIdentification.username") + " " + access.username)
		}
		else {
			tooltips.add(Game.instance.languageManager.getLocal("info.cardIdentification.empty"))
		}
		return tooltips
	}

	override def onRightClick(entity: Entity) {
		super.onRightClick(entity)
		if (Side.get.isServer) {
			if (entity.isInstanceOf[Player]) {
				val player = entity.asInstanceOf[Player]
				if (Game.instance.keyManager.isKeyDown(Key.KEY_LSHIFT)) {

					if (access != null) {
						access = new AccessUser(player.getUsername)
					}
					else {
						access = new AccessUser(player.getUsername)
					}
				}
				else {
					/**
					 * Open item GUI
					 */
					player.openGui(ModularForceFieldSystem, EnumGui.cardID.id, world, 0, 0, 0)
					//Game.instance.guiFactory.showGui()
				}
			}
		}

		return Item
	}

	override def read(packet: Packet) {
		super.read(packet)

		packet.getID match {
			case 0 => {
				/**
				 * Permission toggle packet
				 */
				val perm = Permissions.find(packet.readString())

				if (access == null) {
					access = new AccessUser(player)
				}

				if (perm != null) {
					if (access.permissions.contains(perm)) {
						access.permissions -= perm
					}
					else {
						access.permissions += perm
					}
				}
			}
			case 1 => {
				/**
				 * Username packet
				 */
				if (access != null) {
					access = new AccessUser(packet.readString())
				}
				else {
					access = new AccessUser(packet.readString())
				}
			}
		}
	}
}
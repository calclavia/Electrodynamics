package mffs.security.card

import java.util
import java.util.Optional

import com.resonant.core.access.AccessUser
import mffs.ModularForceFieldSystem
import mffs.item.gui.EnumGui
import nova.core.entity.Entity
import nova.core.game.Game
import nova.core.item.Item
import nova.core.network.PacketHandler
import nova.core.player.Player

class ItemCardIdentification extends ItemCardAccess with PacketHandler {
	override var access = new AccessUser

	override def hitEntity(Item: Item, entityLiving: EntityLivingBase, par3EntityLiving: EntityLivingBase): Boolean = {
		if (entityLiving.isInstanceOf[EntityPlayer]) {
			val access = getAccess(Item)
			access.username = entityLiving.asInstanceOf[EntityPlayer].getGameProfile.getName
			setAccess(Item, access)
		}

		return false
	}

	override def getTooltips(player: Optional[Player]): util.List[String] = {
		val tooltip = super.getTooltips(player)
		val access = getAccess(Item)

		if (access != null) {
			tooltip.add(Game.instance.languageManager.getLocal("info.cardIdentification.username") + " " + access.username)
		}
		else {
			tooltip.add(Game.instance.languageManager.getLocal("info.cardIdentification.empty"))
		}
		return tooltip
	}

	override def getAccess(Item: Item): AccessUser = access

	override def onRightClick(entity: Entity) {
		super.onRightClick(entity)
		if (Game.instance.networkManager.isServer) {
			if (entity.isInstanceOf[Player]) {
				val player = entity.asInstanceOf[Player]
				if (player.issneaking) {
					var access = getAccess(Item)

					if (access != null) {
						access.username = player.getUsername
					}
					else {
						access = new AccessUser(player.getUsername)
					}

					setAccess(Item, access)
				}
				else {
					/**
					 * Open item GUI
					 */
					player.openGui(ModularForceFieldSystem, EnumGui.cardID.id, world, 0, 0, 0)
				}
			}
		}

		return Item
	}

	/**
	 * Reads a packet
	 * @param buf   - data encoded into the packet
	 * @param player - player that is receiving the packet
	 * @param packet - The packet instance that was sending this packet.
	 */
	override def read(buf: Packet, player: EntityPlayer, packet: PacketType) {
		val Item = player.getCurrentEquippedItem
		var access = getAccess(Item)

		buf.readInt() match {
			case 0 => {
				/**
				 * Permission toggle packet
				 */
				val perm = Permissions.find(buf.readString())

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
					access.username = buf.readString()
				}
				else {
					access = new AccessUser(buf.readString())
				}
			}
		}

		setAccess(Item, access)
	}
}
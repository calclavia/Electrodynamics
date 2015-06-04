package com.calclavia.edx.optics.security.card

import com.resonant.core.access.{AbstractAccess, AccessUser, Permissions}
import com.resonant.lib.WrapFunctions._
import nova.core.entity.component.Player
import nova.core.game.Game
import nova.core.gui.InputManager.Key
import nova.core.item.Item.{RightClickEvent, TooltipEvent}
import nova.core.network.NetworkTarget.Side
import nova.core.network.{Packet, PacketHandler}
import nova.core.retention.Stored
import nova.core.util.transform.vector.Vector3i

import scala.beans.BeanProperty

class ItemCardIdentification extends ItemCardAccess with PacketHandler {
	/*
	override def hitEntity(Item: Item, entityLiving: EntityLivingBase, par3EntityLiving: EntityLivingBase): Boolean = {
		if (entityLiving.isInstanceOf[Player]) {
			access = new AccessUser(entityLiving.asInstanceOf[Player].getDisplayName)
		}

		return false
	}*/

	@BeanProperty
	@Stored
	override var access: AbstractAccess = null

	tooltipEvent.add(eventListener((evt: TooltipEvent) => {
		if (access != null) {
			evt.tooltips.add(Game.language.translate("info.cardIdentification.username") + " " + access.asInstanceOf[AccessUser].username)
		}
		else {
			evt.tooltips.add(Game.language.translate("info.cardIdentification.empty"))
		}
	}))

	rightClickEvent.add((evt: RightClickEvent) => {
		if (Side.get.isServer) {
			if (evt.entity.has(classOf[Player])) {
				val player = evt.entity.get(classOf[Player])
				if (Game.input.isKeyDown(Key.KEY_LSHIFT)) {

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
					Game.gui.showGui("idCard", evt.entity, new Vector3i(0, 0, 0))
				}
			}
		}
	})

	override def read(packet: Packet) {
		super.read(packet)

		packet.getID match {
			case 0 => {
				/**
				 * Permission toggle packet
				 */
				val perm = Permissions.find(packet.readString())
				//TODO: Create new access if current doesn't exist.
				if (access != null) {
					if (perm != null) {
						if (access.permissions.contains(perm)) {
							access.permissions -= perm
						}
						else {
							access.permissions += perm
						}
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

	override def getID: String = "cardIdentification"
}
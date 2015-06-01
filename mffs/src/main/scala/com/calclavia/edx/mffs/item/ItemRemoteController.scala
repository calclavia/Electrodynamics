package com.calclavia.edx.mffs.item

import java.util
import java.util.Optional

import com.calclavia.edx.mffs.GraphFrequency
import com.calclavia.edx.mffs.api.MFFSEvent.EventForceMobilize
import com.calclavia.edx.mffs.api.card.CoordLink
import com.calclavia.edx.mffs.base.BlockFortron
import com.calclavia.edx.mffs.item.card.ItemCardFrequency
import com.calclavia.edx.mffs.particle.{FXFortronBeam, FieldColor}
import com.calclavia.edx.mffs.security.MFFSPermissions
import com.calclavia.edx.mffs.util.MFFSUtility
import com.resonant.lib.WrapFunctions._
import com.resonant.wrapper.lib.utility.science.UnitDisplay
import nova.core.block.Block
import nova.core.entity.Entity
import nova.core.entity.component.Player
import nova.core.fluid.Fluid
import nova.core.game.Game
import nova.core.gui.KeyManager.Key
import nova.core.item.Item
import nova.core.item.Item.{RightClickEvent, TooltipEvent, UseEvent}
import nova.core.network.NetworkTarget.Side
import nova.core.retention.{Storable, Stored}
import nova.core.util.Direction
import nova.core.util.collection.Tuple2
import nova.core.util.transform.vector.{Vector3d, Vector3i}
import nova.core.world.World

class ItemRemoteController extends ItemCardFrequency with CoordLink with Storable {
	//TODO: Is this needed?
	private var remotesCached = Set.empty[Item]
	private var temporaryRemoteBlacklist = Set.empty[Item]

	@Stored
	private var linkWorld: World = _
	@Stored
	private var linkPos: Vector3i = _

	tooltipEvent.add(eventListener((evt: TooltipEvent) => {
		if (linkWorld != null) {
			val block = linkWorld.getBlock(linkPos)
			if (block.isPresent) {
				//TODO: Get the real block name?
				evt.tooltips.add(Game.languageManager.translate("info.item.linkedWith") + " " + block.get().getID)
			}
			evt.tooltips.add(linkPos.xi + ", " + linkPos.yi + ", " + linkPos.zi)
			evt.tooltips.add(Game.languageManager.translate("info.item.dimension") + " '" + linkWorld.getID + "'")
		}
		else {
			evt.tooltips.add(Game.languageManager.translate("info.item.notLinked"))
		}
	}))


	useEvent.add((evt: UseEvent) => {
		if (Side.get().isServer && Game.keyManager.isKeyDown(Key.KEY_LSHIFT)) {
			linkWorld = evt.entity.world
			linkPos = evt.position
			val block = linkWorld.getBlock(linkPos).get()

			if (block != null) {
				Game.networkManager.sendChat(evt.entity.asInstanceOf[Player], Game.languageManager.translate("message.remoteController.linked").replaceAll("#p", evt.position.x + ", " + evt.position.y + ", " + evt.position.z).replaceAll("#q", block.getID))
			}
		}
		evt.action = true
	})


	rightClickEvent.add((evt: RightClickEvent) => {
		if (!Game.keyManager.isKeyDown(Key.KEY_LSHIFT)) {
			if (linkPos != null) {
				val op = linkWorld.getBlock(linkPos)

				if (op.isPresent) {
					val block = op.get()

					val player = evt.entity.get(classOf[Player])

					var finished = false
					if (MFFSUtility.hasPermission(linkWorld, linkPos.toDouble, MFFSPermissions.blockAccess, player) || MFFSUtility.hasPermission(linkWorld, linkPos.toDouble, MFFSPermissions.remoteControl, player)) {
						val requiredEnergy = evt.entity.position().distance(linkPos) * (Fluid.bucketVolume / 100)
						var receivedEnergy = 0
						val fortronBlocks = GraphFrequency
							.instance
							.get(frequency)
							.collect { case block: BlockFortron => block }
							.filter(_.position().distance(evt.entity.position()) < 50)

						for (fortronBlock <- fortronBlocks) {
							val consumedEnergy = fortronBlock.removeFortron(Math.ceil(requiredEnergy / fortronBlocks.size).toInt, true)

							if (consumedEnergy > 0) {
								if (Side.get().isServer) {
									val newFX = evt.entity.world.addClientEntity(new FXFortronBeam(FieldColor.blue, 20))
									newFX.setPosition(evt.entity.position /*.add(new Vector3d(0, entity.getEyeHeight - 0.2, 0))*/)
									newFX.setTarget(fortronBlock.position.toDouble.add(0.5))
								}
								receivedEnergy += consumedEnergy
							}

							if (receivedEnergy >= requiredEnergy) {
								try {
									block.rightClickEvent.publish(new Block.RightClickEvent(evt.entity, Direction.UNKNOWN, Vector3d.zero))
								}
								catch {
									case e: Exception => {
										e.printStackTrace()
									}
								}
								finished = true
							}
						}
						if (!finished && Side.get().isServer) {
							Game.networkManager.sendChat(evt.entity.get(classOf[Player]), Game.languageManager.translate("message.remoteController.fail").replaceAll("#p", new
									UnitDisplay(UnitDisplay.Unit.JOULES, requiredEnergy).toString))
						}
					}
				}
			}
		}
	})

	def hasLink: Boolean = linkWorld != null

	def clearLink(Item: Item) {
		linkWorld = null
		linkPos = null
	}
	def getLink: Tuple2[World, Vector3i] = {
		return new Tuple2(linkWorld, linkPos)
	}

	def preMove(evt: EventForceMobilize) {
		this.temporaryRemoteBlacklist = Set.empty
	}

	/**
	 * Moves the coordinates of the link if the Force Manipulator moved a block that is linked by
	 * the remote.
	 *
	 * @param evt
	 */
	def postMove(evt: EventForceMobilize) {
		if (Side.get().isServer) {
			for (item <- this.remotesCached) {
				if (!temporaryRemoteBlacklist.contains(item) && evt.before.equals(linkPos)) {
					setLink(evt.worldAfter, evt.after)
					temporaryRemoteBlacklist += item
				}
			}
		}
	}

	def setLink(world: World, pos: Vector3i) {
		linkWorld = world
		linkPos = pos
	}

	override def getID: String = "remote"
}
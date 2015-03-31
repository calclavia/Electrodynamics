package mffs.item

import java.util
import java.util.Optional

import com.resonant.wrapper.lib.utility.science.UnitDisplay
import mffs.GraphFrequency
import mffs.api.MFFSEvent.EventForceMobilize
import mffs.api.card.CoordLink
import mffs.base.BlockFortron
import mffs.item.card.ItemCardFrequency
import mffs.particle.{FXFortronBeam, FieldColor}
import mffs.security.MFFSPermissions
import mffs.util.MFFSUtility
import nova.core.entity.Entity
import nova.core.fluid.Fluid
import nova.core.game.Game
import nova.core.gui.KeyManager.Key
import nova.core.item.Item
import nova.core.network.NetworkTarget.Side
import nova.core.player.Player
import nova.core.retention.{Storable, Stored}
import nova.core.util.Direction
import nova.core.util.collection.Pair
import nova.core.util.transform.{Vector3d, Vector3i}
import nova.core.world.World

class ItemRemoteController extends ItemCardFrequency with CoordLink with Storable {
	private var remotesCached = Set.empty[Item]
	private var temporaryRemoteBlacklist = Set.empty[Item]

	@Stored
	private var linkWorld: World = _
	@Stored
	private var linkPos: Vector3i = _

	override def getTooltips(player: Optional[Player], tooltips: util.List[String]) {
		super.getTooltips(player, tooltips)

		if (linkWorld != null) {
			val block = linkWorld.getBlock(linkPos)
			if (block.isPresent) {
				//TODO: Get the real block name?
				tooltips.add(Game.instance.languageManager.getLocal("info.item.linkedWith") + " " + block.get().getID)
			}
			tooltips.add(linkPos.xi + ", " + linkPos.yi + ", " + linkPos.zi)
			tooltips.add(Game.instance.languageManager.getLocal("info.item.dimension") + " '" + linkWorld.getID + "'")
		}
		else {
			tooltips.add(Game.instance.languageManager.getLocal("info.item.notLinked"))
		}
	}

	def hasLink: Boolean = linkWorld != null

	override def onUse(entity: Entity, world: World, position: Vector3i, side: Direction, hit: Vector3d): Boolean = {
		super.onUse(entity, world, position, side, hit)

		if (Side.get().isServer && Game.instance.keyManager.isKeyDown(Key.KEY_LSHIFT)) {
			linkWorld = world
			linkPos = position
			val block = linkWorld.getBlock(linkPos).get()

			if (block != null) {
				Game.instance.networkManager.sendChat(entity.asInstanceOf[Player], Game.instance.languageManager.getLocal("message.remoteController.linked").replaceAll("#p", position.x + ", " + position.y + ", " + position.z).replaceAll("#q", block.getID))
			}
		}

		return true
	}

	def clearLink(Item: Item) {
		linkWorld = null
		linkPos = null
	}

	override def onRightClick(entity: Entity) {
		if (!Game.instance.keyManager.isKeyDown(Key.KEY_LSHIFT)) {
			if (linkPos != null) {
				val op = linkWorld.getBlock(linkPos)

				if (op.isPresent) {
					val block = op.get()

					val player = entity.asInstanceOf[Player]
					if (MFFSUtility.hasPermission(linkWorld, linkPos.toDouble, MFFSPermissions.blockAccess, player) || MFFSUtility.hasPermission(linkWorld, linkPos.toDouble, MFFSPermissions.remoteControl, player)) {
						val requiredEnergy = entity.position().distance(linkPos) * (Fluid.bucketVolume / 100)
						var receivedEnergy = 0
						val fortronBlocks = GraphFrequency
							.instance
							.get(frequency)
							.collect { case block: BlockFortron => block }
							.filter(_.position().distance(entity.position()) < 50)

						for (fortronBlock <- fortronBlocks) {
							val consumedEnergy = fortronBlock.removeFortron(Math.ceil(requiredEnergy / fortronBlocks.size).toInt, true)

							if (consumedEnergy > 0) {
								if (Side.get().isServer) {
									val newFX = entity.world.createClientEntity(new FXFortronBeam(FieldColor.blue, 20))
									newFX.setPosition(entity.position /*.add(new Vector3d(0, entity.getEyeHeight - 0.2, 0))*/)
									newFX.setTarget(fortronBlock.position.toDouble.add(0.5))
								}
								receivedEnergy += consumedEnergy
							}
							if (receivedEnergy >= requiredEnergy) {
								try {
									block.onRightClick(entity, 0, Vector3d.zero)
								}
								catch {
									case e: Exception => {
										e.printStackTrace()
									}
								}
								return
							}
						}
						if (Side.get().isServer) {
							Game.instance.networkManager.sendChat(entity.asInstanceOf[Player], Game.instance.languageManager.getLocal("message.remoteController.fail").replaceAll("#p", new
									UnitDisplay(UnitDisplay.Unit.JOULES, requiredEnergy).toString))
						}
					}
				}
			}
		}
	}

	def getLink: Pair[World, Vector3i] = {
		return new Pair(linkWorld, linkPos)
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

}
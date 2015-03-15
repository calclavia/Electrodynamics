package mffs.item

import java.util
import java.util.Optional

import com.resonant.wrapper.lib.utility.science.UnitDisplay
import mffs.api.MFFSEvent.EventForceMobilize
import mffs.api.card.CoordLink
import mffs.base.BlockFortron
import mffs.item.card.ItemCardFrequency
import mffs.particle.FieldColor
import mffs.security.MFFSPermissions
import mffs.util.MFFSUtility
import mffs.{GraphFrequency, ModularForceFieldSystem}
import nova.core.entity.Entity
import nova.core.fluid.Fluid
import nova.core.game.Game
import nova.core.gui.KeyManager.Key
import nova.core.item.Item
import nova.core.network.NetworkTarget.Side
import nova.core.player.Player
import nova.core.retention.{Storable, Stored}
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

		if (hasLink(Item)) {
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

	def hasLink(Item: Item): Boolean = getLink(Item) != null

	override def onItemUse(Item: Item, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, par7: Int, par8: Float, par9: Float, par10: Float): Boolean = {
		if (Game.instance.networkManager.isServer && player.isSneaking) {
			val vector: VectorWorld = new VectorWorld(world, x, y, z)
			setLink(Item, vector)
			val block = vector.getBlock

			if (block != null) {
				player.addChatMessage(new
						ChatComponentText(Game.instance.get.languageManager.getLocal("message.remoteController.linked").replaceAll("#p", x + ", " + y + ", " + z).replaceAll("#q", block.getLocalizedName)))
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
							.collect { case block: BlockFortron => block}
							.filter(_.position().distance(entity.position()) < 50)

						for (fortronBlock <- fortronBlocks) {
							val consumedEnergy = fortronBlock.removeFortron(Math.ceil(requiredEnergy / fortronBlocks.size).toInt, true)

							if (consumedEnergy > 0) {
								if (world.isRemote) {
									ModularForceFieldSystem.proxy.renderBeam(world, new Vector3d(entity).add(new Vector3d(0, entity.getEyeHeight - 0.2, 0)), new
											Vector3d(fortronBlock.asInstanceOf[TileEntity]).add(0.5), FieldColor.blue, 20)
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
								return Item
							}
						}
						if (Side.get().isServer) {
							entity.addChatMessage(new ChatComponentText(Game.instance.languageManager.getLocal("message.remoteController.fail").replaceAll("#p", new
									UnitDisplay(UnitDisplay.Unit.JOULES, requiredEnergy).toString)))
						}
					}
				}
			}
		}
	}

	def getLink(Item: Item): Pair[World, Vector3i] = {
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
			for (Item <- this.remotesCached) {
				if (!temporaryRemoteBlacklist.contains(Item) && evt.before.equals(linkPos)) {
					setLink(evt.worldAfter, evt.after)
					temporaryRemoteBlacklist += Item
				}
			}
		}
	}

	def setLink(world: World, pos: Vector3i) {
		linkWorld = world
		linkPos = pos
	}

}
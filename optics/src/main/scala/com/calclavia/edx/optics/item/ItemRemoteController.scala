package com.calclavia.edx.optics.item

import com.calclavia.edx.optics.GraphFrequency
import com.calclavia.edx.optics.api.MFFSEvent.EventForceMobilize
import com.calclavia.edx.optics.api.card.CoordLink
import com.calclavia.edx.optics.base.BlockFortron
import com.calclavia.edx.optics.item.card.ItemCardFrequency
import com.calclavia.edx.optics.fx.FieldColor
import com.calclavia.edx.optics.beam.fx.EntityMagneticBeam
import com.calclavia.edx.optics.security.MFFSPermissions
import com.calclavia.edx.optics.util.MFFSUtility
import com.resonant.lib.WrapFunctions._
import nova.core.block.Block
import nova.core.entity.component.Player
import nova.core.fluid.Fluid
import com.calclavia.edx.core.EDX
import nova.core.gui.InputManager.Key
import nova.core.item.Item
import nova.core.item.Item.{RightClickEvent, TooltipEvent, UseEvent}
import nova.core.network.NetworkTarget.Side
import nova.core.retention.{Storable, Store}
import nova.core.util.Direction
import nova.core.util.collection.Tuple2
import nova.core.util.transform.vector.{Vector3d, Vector3i}
import nova.core.world.World
import nova.energy.UnitDisplay

class ItemRemoteController extends ItemCardFrequency with CoordLink with Storable {
	//TODO: Is this needed?
	private var remotesCached = Set.empty[Item]
	private var temporaryRemoteBlacklist = Set.empty[Item]

	@Store
	private var linkWorld: World = _
	@Store
	private var linkPos: Vector3i = _

	tooltipEvent.add(eventListener((evt: TooltipEvent) => {
		if (linkWorld != null) {
			val block = linkWorld.getBlock(linkPos)
			if (block.isPresent) {
				//TODO: Get the real block name?
				evt.tooltips.add(EDX.language.translate("info.item.linkedWith") + " " + block.get().getID)
			}
			evt.tooltips.add(linkPos.xi + ", " + linkPos.yi + ", " + linkPos.zi)
			evt.tooltips.add(EDX.language.translate("info.item.dimension") + " '" + linkWorld.getID + "'")
		}
		else {
			evt.tooltips.add(EDX.language.translate("info.item.notLinked"))
		}
	}))


	useEvent.add((evt: UseEvent) => {
		if (Side.get().isServer && EDX.input.isKeyDown(Key.KEY_LSHIFT)) {
			linkWorld = evt.entity.world
			linkPos = evt.position
			val block = linkWorld.getBlock(linkPos).get()

			if (block != null) {
				EDX.network.sendChat(evt.entity.asInstanceOf[Player], EDX.language.translate("message.remoteController.linked").replaceAll("#p", evt.position.x + ", " + evt.position.y + ", " + evt.position.z).replaceAll("#q", block.getID))
			}
		}
		evt.action = true
	})


	rightClickEvent.add((evt: RightClickEvent) => {
		if (!EDX.input.isKeyDown(Key.KEY_LSHIFT)) {
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
									val newFX = evt.entity.world.addClientEntity(new EntityMagneticBeam(FieldColor.blue, 20))
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
							EDX.network.sendChat(evt.entity.get(classOf[Player]), EDX.language.translate("message.remoteController.fail").replaceAll("#p", new
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
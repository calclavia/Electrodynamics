package com.calclavia.edx.optics

import com.calclavia.edx.core.EDX
import com.calclavia.edx.optics.field.BlockProjector
import nova.core.event.BlockEvent
import nova.core.event.bus.GlobalEvents

import scala.collection.convert.wrapAll._

object EventHandler {

	/**
	 * Special stabilization cases.
	 *
	def eventStabilize(evt: EventStabilize) {
		if (evt.Item.getItem.isInstanceOf[ItemSkull]) {
			evt.world.setBlock(evt.x, evt.y, evt.z, Blocks.skull, evt.Item.getItemDamage, 2)
			val tile = evt.world.getTileEntity(evt.x, evt.y, evt.z)

			if (tile.isInstanceOf[TileEntitySkull]) {
				val nbt = evt.Item.getTagCompound

				if (nbt != null) {
					var gameProfile: GameProfile = null

					if (nbt.hasKey("SkullOwner", 10)) {
						gameProfile = NBTUtil.func_152459_a(nbt.getCompoundTag("SkullOwner"))
					}
					else if (nbt.hasKey("SkullOwner", 8) && nbt.getString("SkullOwner").length > 0) {
						gameProfile = new GameProfile(null.asInstanceOf[UUID], nbt.getString("SkullOwner"))
					}

					if (gameProfile != null) {
						tile.asInstanceOf[TileEntitySkull].func_152106_a(gameProfile)
					}
					else {
						tile.asInstanceOf[TileEntitySkull].func_152107_a(evt.Item.getItemDamage)
					}

					Blocks.skull.asInstanceOf[BlockSkull].func_149965_a(evt.world, evt.x, evt.y, evt.z, tile.asInstanceOf[TileEntitySkull])
				}
			}

			evt.Item.stackSize -= 1
			evt.setCanceled(true)
		}
	}

	def playerInteractEvent(evt: PlayerInteractEvent) {
		// Cancel if we click on a force field.
		if (evt.action == Action.LEFT_CLICK_BLOCK && evt.entityPlayer.worldObj.getBlock(evt.x, evt.y, evt.z) == Content.forceField) {
			evt.setCanceled(true)
			return
		}

		// Only check non-creative players
		if (evt.entityPlayer.capabilities.isCreativeMode) {
			return
		}

		val position = new Vector3d(evt.x, evt.y, evt.z)

		val relevantProjectors = MFFSUtility.getRelevantProjectors(evt.entityPlayer.worldObj, position)

		//Check if we can sync this block (activate). If not, we cancel the event.
		if (!relevantProjectors.forall(x => x.isAccessGranted(evt.entityPlayer.worldObj, new Vector3d(evt.x, evt.y, evt.z), evt.entityPlayer, evt.action))) {
			//Check if player has permission
			evt.entityPlayer.addChatMessage(new ChatComponentText("[" + Reference.name + "] You have no permission to do that!"))
			evt.setCanceled(true)
		}
	}

	def livingSpawnEvent(evt: LivingSpawnEvent) {
		if (!evt.entity.isInstanceOf[EntityPlayer]) {
			if (MFFSUtility.getRelevantProjectors(evt.world, new Vector3d(evt.entityLiving)).exists(_.getModuleCount(Content.moduleAntiSpawn) > 0)) {
				evt.setResult(Event.Result.DENY)
			}
		}
	}*/

	/**
	 * When a block breaks, mark force field projectors for an update.
	 */
	def onBlockChange(evt: BlockEvent.Change) {
		if (EDX.network.isServer && evt.newBlock.sameType(EDX.blocks.getAirBlock)) {
			GraphFrequency.instance
				.getNodes
				.view
				.collect { case p: BlockProjector => p }
				.filter(_.world == evt.world)
				.filter(_.getCalculatedField != null)
				.filter(_.getCalculatedField.contains(evt.position))
				.foreach(_.markFieldUpdate = true)
		}
	}

}
package com.calclavia.edx.mffs.base

import java.util.{Optional, Set => JSet}

import com.calclavia.edx.mffs.api.modules.Module
import com.calclavia.edx.mffs.content.Content
import com.calclavia.edx.mffs.util.CacheHandler
import nova.core.fluid.Fluid
import nova.core.game.Game
import nova.core.item.{Item, ItemFactory}
import nova.core.network.Packet

abstract class BlockModuleHandler extends BlockFortron with CacheHandler {

	lazy val endModuleIndex = inventory.size() - 1
	var startModuleIndex = 1
	/**
	 * Client side only.
	 */
	var clientFortronCost = 0

	protected var capacityBase = 500
	protected var capacityBoost = 5

	override def write(packet: Packet) {
		super.write(packet)

		if (packet.getID == PacketBlock.description) {
			packet <<< getFortronCost
		}
	}

	/**
	 * Returns Fortron cost in ticks.
	 */
	final def getFortronCost: Int = {
		if (Game.networkManager.isClient) {
			return clientFortronCost
		}

		return getOrSetCache("getFortronCost", doGetFortronCost)
	}

	protected def doGetFortronCost(): Int = Math.round(getModules().foldLeft(0f)((a, b) => a + b.count * b.asInstanceOf[Module].getFortronCost(getAmplifier)))

	protected def getAmplifier: Float = 1f

	/**
	 * Gets all the modules in this block that are in specific slots
	 * @param slots The slot IDs. Providing null will search all slots
	 * @return The set of all item modules in the slots.
	 */
	def getModules(slots: Int*): Set[Item with Module] =
		getOrSetCache(
			"getModules_" + (if (slots != null) slots.hashCode() else ""),
			() => {
				val iterSlots = if (slots == null || slots.length <= 0) startModuleIndex until endModuleIndex else slots

				return iterSlots
					.view
					.map(inventory.get)
					.collect { case item: Optional[Item] if item.isPresent => item.get() }
					.collect { case item: Item with Module => item }
					.toSet
			}
		)

	override def read(packet: Packet) {
		super.read(packet)

		if (packet.getID == PacketBlock.description) {
			clientFortronCost = packet.readInt()
		}
	}

	override def start() {
		super.start()
		refresh()
	}

	def refresh() {
		fortronTank.setCapacity((this.getModuleCount(Content.moduleCapacity) * this.capacityBoost + this.capacityBase) * Fluid.bucketVolume)
	}

	/**
	 * Gets the number of modules in this block that are in specific slots
	 * @param slots The slot IDs. Providing null will search all slots
	 * @return The number of all item modules in the slots.
	 */

	def getModuleCount(compareModule: ItemFactory, slots: Int*): Int =
		getOrSetCache(
			"getModuleCount_" + compareModule.hashCode + (if (slots != null) slots.hashCode() else ""),
			() => {
				val iterSlots = if (slots == null || slots.length <= 0) startModuleIndex until endModuleIndex else slots

				return iterSlots
					.view
					.map(inventory.get)
					.collect { case item: Optional[Item] if item.isPresent => item.get() }
					.collect { case item: Item with Module if compareModule.sameType(item) => item }
					.foldLeft(0)(_ + _.count)
			}
		)

	def markDirty() {
		refresh()
		clearCache()
	}

	def consumeCost() {
		removeFortron(getFortronCost, true)
	}

	/**
	 * Gets the module, if it exists, in this block based on a compareModule.
	 * @param compareModule The module to compare against
	 * @return Null if no such module exists
	 */
	def getModule(compareModule: Item): Item with Module =
		getOrSetCache(
			"getModule_" + compareModule.hashCode,
			() => {
				val modules = getModules()

				if (modules.size > 0) {
					return compareModule.withAmount(modules.count(compareModule.sameItemType)).asInstanceOf[Item with Module]
				}
				else {
					return null
				}
			}
		)
}
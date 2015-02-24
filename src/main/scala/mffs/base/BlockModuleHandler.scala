package mffs.base

import java.util.{Optional, Set => JSet}

import mffs.api.modules.Module
import mffs.content.Content
import mffs.util.CacheHandler
import nova.core.fluid.Fluid
import nova.core.game.Game
import nova.core.item.Item
import nova.core.network.Packet

abstract class BlockModuleHandler extends BlockFortron with CacheHandler {

	var startModuleIndex = 1
	var endModuleIndex = inventory.size() - 1

	/**
	 * Client side only.
	 */
	var clientFortronCost = 0

	protected var capacityBase = 500
	protected var capacityBoost = 5

	override def write(id: Int, packet: Packet) {
		super.write(id, packet)

		if (id == PacketBlock.description.ordinal()) {
			packet <<< getFortronCost
		}
	}

	override def read(id: Int, packet: Packet) {
		super.read(id, packet)

		if (id == PacketBlock.description.ordinal()) {
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

	def markDirty() {
		refresh()
		clearCache()
	}

	def consumeCost() {
		removeFortron(getFortronCost, true)
	}

	/**
	 * Returns Fortron cost in ticks.
	 */
	final def getFortronCost: Int = {
		if (Game.instance.networkManager.isClient) {
			return clientFortronCost
		}

		return getOrSetCache("getFortronCost", doGetFortronCost)
	}

	protected def doGetFortronCost(): Int =
		Math.round(
			getModules()
				.foldLeft(0f)((a, b) => a + b.count * b.asInstanceOf[Module].getFortronCost(getAmplifier))
		)

	protected def getAmplifier: Float = 1f

	/**
	 * Gets the number of modules in this block that are in specific slots
	 * @param slots The slot IDs. Providing null will search all slots
	 * @return The number of all item modules in the slots.
	 */
	def getModuleCount(compareModule: Item, slots: Int*): Int =
		getOrSetCache(
			"getModuleCount_" + compareModule.hashCode + (if (slots != null) slots.hashCode() else ""),
			() => {
				val iterSlots = if (slots == null || slots.length <= 0) startModuleIndex until endModuleIndex else slots

				return iterSlots
					.view
					.map(inventory.get)
					.collect { case item: Optional[Item] if item.isPresent => item.get()}
					.collect { case item: Item with Module if compareModule.sameItemType(item) => item}
					.foldLeft(0)(_ + _.count)
			}
		)

	/**
	 * Gets the module, if it exists, in this block based on a compareModule.
	 * @param compareModule The module to compare against
	 * @return Null if no such module exists
	 */
	def getModule(compareModule: Item): Item =
		getOrSetCache(
			"getModule_" + compareModule.hashCode,
			() => {
				val modules = getModules()

				if (modules.size > 0) {
					compareModule.withAmount(modules.count(compareModule.sameItemType))
				}
				else {
					null
				}
			}
		)

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
					.collect { case item: Optional[Item] if item.isPresent => item.get()}
					.collect { case item: Item with Module => item}
					.toSet
			}
		)
}
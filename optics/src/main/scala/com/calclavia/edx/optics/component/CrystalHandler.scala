package com.calclavia.edx.optics.component

import java.util.{Optional, Set => JSet}

import com.calclavia.edx.optics.api.modules.Module
import com.calclavia.edx.optics.util.CacheHandler
import nova.core.block.Block
import nova.core.component.Component
import nova.core.component.inventory.Inventory
import nova.core.item.{Item, ItemFactory}

/**
 * Handles crystal modules
 */
//@Require(classOf[Inventory])
class CrystalHandler(val block: Block) extends Component with CacheHandler {

	private lazy val inventory = block.components.get(classOf[Inventory])
	private lazy val endModuleIndex = inventory.size() - 1
	var startModuleIndex = 0
	var capacityBase = 500
	var capacityBoost = 5

	def powerCost = getOrSetCache("energyCost", () => doGetEnergyCost)

	protected def doGetEnergyCost = getModules().foldLeft(0f)((a, b) => a + b.count * b.asInstanceOf[Module].getFortronCost(amplifier.toFloat))

	protected def amplifier = 1d

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
}
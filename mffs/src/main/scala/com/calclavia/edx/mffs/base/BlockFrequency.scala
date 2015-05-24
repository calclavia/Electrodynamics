package com.calclavia.edx.mffs.base

import java.util.{Set => JSet}

import com.calclavia.edx.mffs.GraphFrequency
import com.calclavia.edx.mffs.api.Frequency
import com.calclavia.edx.mffs.item.card.ItemCardFrequency
import com.resonant.core.prefab.block.InventorySimpleProvider
import nova.core.item.Item

/**
 * All blocks that have a frequency value should extend this
 * @author Calclavia
 */
abstract class BlockFrequency extends BlockMachine with Frequency with InventorySimpleProvider {
	val frequencySlot = 0

	override def load() {
		GraphFrequency.instance.add(this)
	}

	override def unload() {
		GraphFrequency.instance.remove(this)
	}

	override def getFrequency: Int = {
		val frequencyCard = getFrequencyCard
		return if (frequencyCard != null) frequencyCard.getFrequency else 0
	}

	override def setFrequency(frequency: Int) {
		val frequencyCard = getFrequencyCard
		if (frequencyCard != null) {
			frequencyCard.setFrequency(frequency)
		}
	}

	def getFrequencyCard: ItemCardFrequency = {
		val item = inventory.get(frequencySlot)

		if (item.isPresent && item.get().isInstanceOf[ItemCardFrequency]) {
			return item.get().asInstanceOf[ItemCardFrequency]
		}

		return null
	}

	/**
	 * Gets a set of cards that define frequency or link connections.
	 */
	def getConnectionCards: Set[Item] = Set(inventory.get(0).orElseGet(null))
}
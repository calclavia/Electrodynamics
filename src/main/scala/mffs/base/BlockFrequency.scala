package mffs.base

import java.util.{Set => JSet}

import com.resonant.core.access.Permission
import mffs.GraphFrequency
import mffs.api.Frequency
import mffs.api.card.ICoordLink
import mffs.item.card.ItemCardFrequency
import mffs.security.{BlockBiometric, MFFSPermissions}
import nova.core.entity.Entity
import nova.core.item.Item
import nova.core.player.Player
import nova.core.util.transform.Vector3d

/**
 * All blocks that have a frequency value should extend this
 * @author Calclavia
 */
abstract class BlockFrequency extends BlockInventory with Frequency {
	val frequencySlot = 0

	override def load() {
		super.load()
		GraphFrequency.instance.add(this)
	}

	override def unload() {
		super.unload()
		GraphFrequency.instance.remove(this)
	}

	def hasPermission(playerID: String, permissions: Permission*): Boolean = permissions.forall(hasPermission(playerID, _))

	def hasPermission(playerID: String, permission: Permission): Boolean = !isActive || getBiometricIdentifiers.forall(_.hasPermission(playerID, permission))

	/**
	 * Gets the first linked biometric identifier, based on the card slots and frequency.
	 */
	def getBiometricIdentifier: BlockBiometric = if (getBiometricIdentifiers.size > 0) getBiometricIdentifiers.head else null

	def getBiometricIdentifiers: Set[BlockBiometric] = {
		val cardLinks = getConnectionCards.view
			.filter(item => item != null && item.isInstanceOf[ICoordLink])
			.map(item => item.asInstanceOf[ICoordLink].getLink())
			.filter(_ != null)
			.filter(_.isInstanceOf[BlockBiometric])
			.map(_.asInstanceOf[BlockBiometric])
			.force
			.toSet

		val frequencyLinks = GraphFrequency.instance.get(getFrequency).filter(_.isInstanceOf[BlockBiometric]).toSet[BlockBiometric]

		return frequencyLinks ++ cardLinks
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

	override def onRightClick(entity: Entity, side: Int, hit: Vector3d): Boolean = {
		if (entity.isInstanceOf[Player]) {
			if (!hasPermission(entity.asInstanceOf[Player].getID, MFFSPermissions.configure)) {
				//TODO: Add chat
				//player.addChatMessage(new ChatComponentText("[" + Reference.name + "]" + " Access denied!"))
				return false
			}
		}

		return super.onRightClick(entity, side, hit)
	}
}
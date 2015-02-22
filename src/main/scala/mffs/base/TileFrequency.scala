package mffs.base

import java.util.{Set => JSet}

import com.resonant.core.access.Permission
import mffs.api.Frequency
import mffs.api.card.ICoordLink
import mffs.item.card.ItemCardFrequency
import mffs.security.{BlockBiometric, MFFSPermissions}
import mffs.{GraphFrequency, Reference}
import nova.core.entity.Entity
import nova.core.item.Item
import nova.core.util.transform.Vector3d

abstract class TileFrequency extends TileMFFSInventory with Frequency {
	val frequencySlot = 0

	override def load() {
		super.load()
		GraphFrequency.instance.add(this)
	}

	override def unload() {
		super.unload()
		GraphFrequency.instance.remove(this)
	}

	def hasPermission(profile: GameProfile, permissions: Permission*): Boolean = permissions.forall(hasPermission(profile, _))

	def hasPermission(profile: GameProfile, permission: Permission): Boolean = !isActive || getBiometricIdentifiers.forall(_.hasPermission(profile, permission))

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

		if (frequencyCard != null) {
			return frequencyCard.asInstanceOf[ItemCardFrequency].getFrequency(frequencyCard)
		}

		return 0
	}

	override def setFrequency(frequency: Int) {

	}

	def getFrequencyCard: ItemCardFrequency = {
		val stack = inventory.get(frequencySlot)

		if (stack != null && stack.isInstanceOf[ItemCardFrequency]) {
			return stack.asInstanceOf[ItemCardFrequency]
		}

		return null
	}

	/**
	 * Gets a set of cards that define connections.
	 */
	def getConnectionCards: Set[Item] = Set(inventory.get(0).orElseGet(null))

	/**
	 * Gets the first linked biometric identifier, based on the card slots and frequency.
	 */
	def getBiometricIdentifier: BlockBiometric = if (getBiometricIdentifiers.size > 0) getBiometricIdentifiers.head else null

	override def onRightClick(entity: Entity, side: Int, hit: Vector3d): Boolean = {
		if (!hasPermission(player.getGameProfile, MFFSPermissions.configure)) {
			player.addChatMessage(new ChatComponentText("[" + Reference.name + "]" + " Access denied!"))
			return false
		}

		return super.onRightClick(entity, side, hit)
	}
}
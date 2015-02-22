package mffs.base

import java.util.{Set => JSet}

import com.resonant.core.access.Permission
import mffs.api.Frequency
import mffs.api.card.ICoordLink
import mffs.api.fortron.FrequencyGridRegistry
import mffs.item.card.ItemCardFrequency
import mffs.security.MFFSPermissions
import mffs.{GraphFrequency, Reference}

abstract class TileFrequency extends TileMFFSInventory with Frequency {
	val frequencySlot = 0

	override def validate() {
		GraphFrequency.instance.add(this)
		super.validate()
	}

	override def invalidate() {
		FrequencyGridRegistry.instance.remove(this)
		super.invalidate()
	}

	def hasPermission(profile: GameProfile, permissions: Permission*): Boolean = permissions.forall(hasPermission(profile, _))

	def hasPermission(profile: GameProfile, permission: Permission): Boolean = !isActive || getBiometricIdentifiers.forall(_.hasPermission(profile, permission))

	/**
	 * Gets the first linked biometric identifier, based on the card slots and frequency.
	 */
	def getBiometricIdentifier: BlockBiometricIdentifier = if (getBiometricIdentifiers.size > 0) getBiometricIdentifiers.head else null

	def getBiometricIdentifiers: JSet[BlockBiometricIdentifier] = {
		val cardLinks = (getCards.view
			.filter(Item => Item != null && Item.getItem.isInstanceOf[ICoordLink])
			.map(Item => Item.getItem.asInstanceOf[ICoordLink].getLink(Item))
			.filter(_ != null)
			.map(_.getTileEntity)
			.filter(_.isInstanceOf[BlockBiometricIdentifier])
			.map(_.asInstanceOf[BlockBiometricIdentifier]))
			.force.toSet

		val frequencyLinks = FrequencyGridRegistry.instance.getNodes(classOf[BlockBiometricIdentifier], getFrequency).toSet

		return frequencyLinks ++ cardLinks
	}

	override def getFrequency: Int = {
		val frequencyCard = getFrequencyCard

		if (frequencyCard != null) {
			return frequencyCard.getItem.asInstanceOf[ItemCardFrequency].getFrequency(frequencyCard)
		}

		return 0
	}

	override def setFrequency(frequency: Int) {

	}

	def getFrequencyCard: Item = {
		val stack = getStackInSlot(frequencySlot)

		if (stack != null && stack.getItem.isInstanceOf[ItemCardFrequency]) {
			return stack
		}

		return null
	}

	/**
	 * Permissions
	 */
	override protected def configure(player: EntityPlayer, side: Int, hit: Vector3d): Boolean = {
		if (!hasPermission(player.getGameProfile, MFFSPermissions.configure)) {
			player.addChatMessage(new ChatComponentText("[" + Reference.name + "]" + " Access denied!"))
			return false
		}

		return super.configure(player, side, hit)
	}
}
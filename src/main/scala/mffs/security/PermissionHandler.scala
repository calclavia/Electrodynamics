package mffs.security

import com.resonant.core.access.Permission
import mffs.GraphFrequency
import mffs.api.card.CoordLink
import mffs.base.BlockFrequency
import nova.core.entity.Entity
import nova.core.player.Player
import nova.core.util.transform.Vector3d

/**
 * @author Calclavia
 */
trait PermissionHandler extends BlockFrequency {

	final def hasPermission(playerID: String, permissions: Permission*): Boolean = permissions.forall(hasPermission(playerID, _))

	def hasPermission(playerID: String, permission: Permission): Boolean = !isActive || getBiometricIdentifiers.forall(_.hasPermission(playerID, permission))

	/**
	 * Gets the first linked biometric identifier, based on the card slots and frequency.
	 */
	def getBiometricIdentifier: BlockBiometric = if (getBiometricIdentifiers.size > 0) getBiometricIdentifiers.head else null

	def getBiometricIdentifiers: Set[BlockBiometric] = {
		val cardLinks = getConnectionCards.view
			.filter(item => item != null && item.isInstanceOf[CoordLink])
			.map(item => item.asInstanceOf[CoordLink].getLink())
			.filter(_ != null)
			.filter(_.isInstanceOf[BlockBiometric])
			.map(_.asInstanceOf[BlockBiometric])
			.force
			.toSet

		val frequencyLinks = GraphFrequency.instance.get(getFrequency).filter(_.isInstanceOf[BlockBiometric]).toSet[BlockBiometric]

		return frequencyLinks ++ cardLinks
	}

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

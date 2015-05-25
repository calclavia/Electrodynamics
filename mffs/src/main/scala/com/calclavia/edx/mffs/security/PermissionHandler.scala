package com.calclavia.edx.mffs.security

import com.calclavia.edx.mffs.GraphFrequency
import com.calclavia.edx.mffs.api.card.CoordLink
import com.calclavia.edx.mffs.base.BlockFrequency
import com.resonant.core.access.Permission
import com.resonant.lib.wrapper.WrapFunctions._
import nova.core.block.Block.RightClickEvent
import nova.core.entity.component.Player
/**
 * @author Calclavia
 */
trait PermissionHandler extends BlockFrequency {

	rightClickEvent.add((evt: RightClickEvent) => onRightClick(evt))

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
			.map(link => link._1.getBlock(link._2).get())
			.collect { case b: BlockBiometric => b }
			.force
			.toSet

		val frequencyLinks = GraphFrequency.instance.get(getFrequency).collect { case b: BlockBiometric => b }

		return frequencyLinks ++ cardLinks
	}

	override def onRightClick(evt: RightClickEvent) {
		val opPlayer = evt.entity.getOp(classOf[Player])
		if (opPlayer.isPresent) {
			if (!hasPermission(opPlayer.get().getPlayerID, MFFSPermissions.configure)) {
				//TODO: Add chat
				//player.addChatMessage(new ChatComponentText("[" + Reference.name + "]" + " Access denied!"))
				evt.result = false
			}
		}
	}
}

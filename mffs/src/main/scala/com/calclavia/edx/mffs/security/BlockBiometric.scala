package com.calclavia.edx.mffs.security

import java.util.{Set => JSet}

import com.calclavia.edx.mffs.api.card.AccessCard
import com.calclavia.edx.mffs.base.BlockFrequency
import com.calclavia.edx.mffs.content.{Models, Textures}
import com.resonant.core.access.Permission
import com.resonant.core.prefab.block.{Rotatable, Updater}
import nova.core.block.components.{DynamicRenderer, StaticRenderer}
import nova.core.entity.Entity
import nova.core.game.Game
import nova.core.inventory.InventorySimple
import nova.core.item.Item
import nova.core.render.model.Model

import scala.collection.convert.wrapAll._

object BlockBiometric {
	val SLOT_COPY = 12
}

class BlockBiometric extends BlockFrequency with Rotatable with Updater with PermissionHandler with StaticRenderer with DynamicRenderer {

	/**
	 * 2 slots: Card copying
	 * 9 x 4 slots: Access Cards
	 * Under access cards we have a permission selector
	 */
	override val inventory = new InventorySimple(1 + 45)

	/**
	 * Rendering
	 */
	var lastFlicker = 0L

	override def update(deltaTime: Double) {
		super.update(deltaTime)
		animation += deltaTime
	}

	override def hasPermission(playerID: String, permission: Permission): Boolean = {
		super.hasPermission(playerID, permission)

		if (!isActive /*|| ModularForceFieldSystem.proxy.isOp(profile) && Settings.allowOpOverride*/ ) {
			return true
		}

		return getConnectionCards
			.map(stack => stack.asInstanceOf[AccessCard].getAccess)
			.filter(_ != null)
			.exists(_.hasPermission(playerID, permission))
	}

	override def getConnectionCards: Set[Item] = inventory.filter(_ != null).filter(_.isInstanceOf[AccessCard]).toSet

	/*
	override def isItemValidForSlot(slotID: Int, Item: Item): Boolean = {
		if (slotID == 0) {
			return Item.getItem.isInstanceOf[ItemCardFrequency]
		}

		return Item.getItem.isInstanceOf[IAccessCard]
	}

	override def getInventoryStackLimit: Int = 1*/

	override def getBiometricIdentifiers: Set[BlockBiometric] = Set(this)

	override def isCube: Boolean = false

	override def renderDynamic(model: Model) {
		model.rotate(direction.rotation)
		/**
		 * Simulate flicker and, hovering
		 */
		val t = System.currentTimeMillis()
		val dist = position.distance(Game.instance.clientManager.getPlayer.asInstanceOf[Entity].position)

		if (dist < 3) {
			if (Math.random() > 0.05 || (lastFlicker - t) > 200) {
				model.translate(0, Math.sin(Math.toRadians(animation)) * 0.05, 0)
				//RenderUtility.enableBlending()
				val screenModel = Models.biometric.getModel
				screenModel.children.removeAll(screenModel.filterNot(_.name.equals("holoScreen")))
				model.children.add(screenModel)
				//RenderUtility.disableBlending()
				lastFlicker = t
			}
		}

		model.bindAll(if (isActive) Textures.biometricOn else Textures.biometricOff)
	}

	override def renderStatic(model: Model) {
		model.rotate(direction.rotation)
		val modelBiometric: Model = Models.biometric.getModel
		modelBiometric.children.removeAll(modelBiometric.children.filter(_.name.equals("holoScreen")))
		model.children.add(modelBiometric)
		model.bindAll(if (isActive) Textures.biometricOn else Textures.biometricOff)
	}

	override def getID: String = "biometric"

}
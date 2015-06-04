package com.calclavia.edx.optics.security

import java.util.{Set => JSet}

import com.calclavia.edx.optics.api.card.AccessCard
import com.calclavia.edx.optics.base.BlockFrequency
import com.calclavia.edx.optics.content.{Models, Textures}
import com.resonant.core.access.Permission
import com.resonant.lib.WrapFunctions._
import nova.core.block.Block.{BlockPlaceEvent, RightClickEvent}
import nova.core.block.component.StaticBlockRenderer
import nova.core.component.renderer.DynamicRenderer
import nova.core.component.transform.Orientation
import nova.core.game.Game
import nova.core.inventory.InventorySimple
import nova.core.item.Item
import nova.core.render.model.Model
import nova.scala.ExtendedUpdater

import scala.collection.convert.wrapAll._

object BlockBiometric {
	val SLOT_COPY = 12
}

class BlockBiometric extends BlockFrequency with ExtendedUpdater with PermissionHandler {

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

	add(new Orientation(this)).hookBlockEvents()

	get(classOf[StaticBlockRenderer])
		.setOnRender(
	    (model: Model) => {
		    model.rotate(get(classOf[Orientation]).orientation.rotation)
		    val modelBiometric: Model = Models.biometric.getModel
		    modelBiometric.children.removeAll(modelBiometric.children.filter(_.name.equals("holoScreen")))
		    model.children.add(modelBiometric)
		    model.bindAll(if (isActive) Textures.biometricOn else Textures.biometricOff)
	    }
		)

	add(new DynamicRenderer())
		.setOnRender(
	    (model: Model) => {
		    model.rotate(get(classOf[Orientation]).orientation.rotation)
		    /**
		     * Simulate flicker and, hovering
		     */
		    val t = System.currentTimeMillis()
		    val dist = position.distance(Game.clientManager.getPlayer.position)

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
		)

	placeEvent.add((evt: BlockPlaceEvent) => world.markStaticRender(position))
	rightClickEvent.add((evt: RightClickEvent) => world.markStaticRender(position))

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

	override def getID: String = "biometric"

}
package mffs.security

import java.util.{Set => JSet}

import com.resonant.core.access.Permission
import com.resonant.core.prefab.block.{Rotatable, Updater}
import mffs.api.card.AccessCard
import mffs.base.BlockFrequency
import mffs.content.{Models, Textures}
import nova.core.inventory.InventorySimple
import nova.core.item.Item
import nova.core.render.model.Model
import nova.core.util.transform.MatrixStack

import scala.collection.convert.wrapAll._

object BlockBiometric {
	val SLOT_COPY = 12
}

class BlockBiometric extends BlockFrequency with Rotatable with Updater with PermissionHandler {

	/**
	 * 2 slots: Card copying
	 * 9 x 4 slots: Access Cards
	 * Under access cards we have a permission selector
	 */
	override
	protected val inventory = new InventorySimple(1 + 45)
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

	override def renderItem(model: Model) = renderDynamic(model)

	override def renderDynamic(model: Model) {
		model.matrix = new MatrixStack()
			.loadMatrix(model.matrix)
			.translate(0, 0.15, 0)
			.scale(1.3, 1.3, 1.3)
			.rotate(direction.rotation)
			.getMatrix

		model.children.add(Models.biometric.getModel)

		/**
		 * Simulate flicker and, hovering
		 */
		val t = System.currentTimeMillis()

		/*
		val look = Minecraft.getMinecraft.thePlayer.rayTrace(8, 1)

		if (look != null && tile.position.equals(new Vector3d(look).floor))
		{
			if (Math.random() > 0.05 || (tile.lastFlicker - t) > 200)
			{
				glPushMatrix()
				glTranslated(0, Math.sin(Math.toRadians(tile.animation)) * 0.05, 0)
				RenderUtility.enableBlending()
				model.renderOnly("holoScreen")
				RenderUtility.disableBlending()
				glPopMatrix()
				tile.lastFlicker = t
			}
		}
		*/
		if (isActive) {
			model.bindAll(Textures.biometricOn)
		}
		else {
			model.bindAll(Textures.biometricOff)
		}
	}

	override def getID: String = "biometric"

}
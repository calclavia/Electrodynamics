package com.calclavia.edx.mffs.field

import java.util
import java.util.Optional

import com.calclavia.edx.mffs.api.machine.ForceField
import com.calclavia.edx.mffs.api.modules.Module
import com.calclavia.edx.mffs.content.{Content, Textures}
import com.calclavia.edx.mffs.security.MFFSPermissions
import com.calclavia.edx.mffs.util.MFFSUtility
import nova.core.block.Block
import nova.core.block.component.{BlockCollider, LightEmitter}
import nova.core.component.renderer.StaticRenderer
import nova.core.entity.Entity
import nova.core.entity.component.Damageable
import nova.core.game.Game
import nova.core.item.Item
import nova.core.network.{PacketHandler, Sync}
import nova.core.player.Player
import nova.core.render.model.Model
import nova.core.render.texture.Texture
import nova.core.retention.{Storable, Stored}
import nova.core.util.Direction
import nova.core.util.transform.shape.Cuboid
import nova.core.util.transform.vector.Vector3i

import scala.collection.convert.wrapAll._

class BlockForceField extends Block with PacketHandler with ForceField with Storable {

	@Stored
	@Sync
	private var camoBlock: Block = null
	@Stored
	@Sync
	private var projector: Vector3i = null

	/**
	 * Constructor
	 */
	add(new BlockCollider(this) {
		override def getCollidingBoxes(intersect: Cuboid, entity: Optional[Entity]): util.Set[Cuboid] = {
			val projector = getProjector()

			if (projector != null && entity.isPresent && entity.get.isInstanceOf[Player]) {
				val biometricIdentifier = projector.getBiometricIdentifier
				val entityPlayer = entity.get.asInstanceOf[Player]

				if (biometricIdentifier != null) {
					if (biometricIdentifier.hasPermission(entityPlayer.getID, MFFSPermissions.forceFieldWarp)) {
						return null
					}
				}
			}
			super.getCollidingBoxes(intersect, entity)
		}

		override def onEntityCollide(entity: Entity) {
			val projector = getProjector()

			if (projector != null) {
				if (!projector.getModules().forall(stack => stack.asInstanceOf[Module].onFieldCollide(BlockForceField.this, entity))) {
					return
				}

				val biometricIdentifier = projector.getBiometricIdentifier

				if ((position().toDouble + 0.5).distance(entity.position()) < 0.5) {
					if (Game.instance.networkManager.isServer && entity.isInstanceOf[Damageable]) {
						val entityLiving = entity.asInstanceOf[Damageable]

						if (entity.isInstanceOf[Player]) {
							val player = entity.asInstanceOf[Player]

							if (biometricIdentifier != null) {
								if (biometricIdentifier.hasPermission(player.getID, MFFSPermissions.forceFieldWarp)) {
									return
								}
							}
						}
					}
				}
			}

		}
	}.setCube(false))

	add(new LightEmitter() {
		override def getEmittedLightLevel: Float = {
			val projector = getProjectorSafe
			if (projector != null) {
				return Math.min(projector.getModuleCount(Content.moduleGlow), 64) / 64f
			}

			return 0
		}
	})

	add(new StaticRenderer(this) {
		override def renderStatic(model: Model) {
			val opRenderer = camoBlock.getComponent(classOf[StaticRenderer])

			if (opRenderer.isPresent)
				opRenderer.get.renderStatic(model)
			else
				super.renderStatic(model)
		}
	})

	override def getHardness: Double = Double.PositiveInfinity

	override def getResistance: Double = Double.PositiveInfinity

	override def getDrops: util.Set[Item] = Set.empty[Item]

	override def shouldRenderSide(side: Direction): Boolean = {
		if (camoBlock != null) {
			try {
				return camoBlock.shouldRenderSide(side)
			}
			catch {
				case e: Exception =>
					e.printStackTrace()
			}
			return true
		}

		val block = world.getBlock(position + side.toVector)
		return if (block.isPresent) sameType(block.get()) else true
	}

	override def getTexture(side: Direction): Optional[Texture] = Optional.of(Textures.forceField)

	override def getID: String = "forceField"

	override def weakenForceField(energy: Int) {
		val projector = getProjector

		if (projector != null) {
			projector.addFortron(energy, true)
		}

		if (Game.instance.networkManager.isServer) {
			world.removeBlock(position)
		}
	}

	/**
	 * @return Gets the projector block controlling this force field. Removes the force field if no
	 *         projector can be found.
	 */
	def getProjector: BlockProjector = {
		if (this.getProjectorSafe != null) {
			return getProjectorSafe
		}

		if (Game.instance.networkManager.isServer) {
			world.removeBlock(position)
		}

		return null
	}

	def getProjectorSafe: BlockProjector = {
		if (projector != null) {

			val projBlock = world.getBlock(projector)
			if (projBlock.isPresent) {
				val proj = projBlock.get().asInstanceOf[BlockProjector]
				if (Game.instance.networkManager.isClient || (proj.getCalculatedField != null && proj.getCalculatedField.contains(position))) {
					return proj
				}
			}
		}

		return null
	}

	def setProjector(position: Vector3i) {
		projector = position

		if (Game.instance.networkManager.isServer) {
			refreshCamoBlock()
		}
	}

	def refreshCamoBlock() {
		if (getProjectorSafe != null) {
			camoBlock = MFFSUtility.getCamoBlock(getProjector, position).getDummy
		}
	}
}
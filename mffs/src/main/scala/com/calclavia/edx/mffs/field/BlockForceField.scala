package com.calclavia.edx.mffs.field

import java.util
import java.util.Optional

import com.calclavia.edx.mffs.api.machine.ForceField
import com.calclavia.edx.mffs.api.modules.Module
import com.calclavia.edx.mffs.content.{Content, Textures}
import com.calclavia.edx.mffs.security.MFFSPermissions
import com.calclavia.edx.mffs.util.MFFSUtility
import com.resonant.lib.wrapper.WrapFunctions._
import nova.core.block.Block
import nova.core.block.component.{BlockCollider, LightEmitter, StaticBlockRenderer}
import nova.core.component.misc.Damageable
import nova.core.component.renderer.StaticRenderer
import nova.core.entity.Entity
import nova.core.entity.component.Player
import nova.core.game.Game
import nova.core.item.Item
import nova.core.network.{PacketHandler, Sync}
import nova.core.render.model.Model
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

			if (projector != null && entity.isPresent && entity.get.get(classOf[Player]).isPresent) {
				val biometricIdentifier = projector.getBiometricIdentifier
				val entityPlayer = entity.get.get(classOf[Player]).get()

				if (biometricIdentifier != null) {
					if (biometricIdentifier.hasPermission(entityPlayer.getPlayerID, MFFSPermissions.forceFieldWarp)) {
						return null
					}
				}
			}
			super.getCollidingBoxes(intersect, entity)
		}
	}
		.setCube(false)
		.setEntityCollide(consumer(
		(entity: Entity) => {
			val projector = getProjector()
			if (projector != null) {
				if (projector.getModules().forall(stack => stack.asInstanceOf[Module].onFieldCollide(BlockForceField.this, entity))) {
					val biometricIdentifier = projector.getBiometricIdentifier

					if ((transform.position.toDouble + 0.5).distance(entity.transform.position) < 0.5) {
						if (Game.instance.networkManager.isServer && entity.isInstanceOf[Damageable]) {
							val entityLiving = entity.asInstanceOf[Damageable]

							if (entity.get(classOf[Player]).isPresent) {
								val player = entity.get(classOf[Player]).get

								if (biometricIdentifier != null) {
									if (biometricIdentifier.hasPermission(player.getID, MFFSPermissions.forceFieldWarp)) {
										//Hurt player?
									}
								}
							}
						}
					}
				}
			}
		}))
	)

	add(new LightEmitter().setEmittedLevel(supplier(() => {
		val projector = getProjectorSafe
		if (projector != null) {
			Math.min(projector.getModuleCount(Content.moduleGlow), 64) / 64f
		}
		0f
	})))

	add(new StaticBlockRenderer(this) {
		override def renderStatic(model: Model) {
			val opRenderer = camoBlock.get(classOf[StaticRenderer])

			if (opRenderer.isPresent)
				opRenderer.get.renderStatic(model)
			else
				super.renderStatic(model)
		}
	}
		.setRenderSide(
	    func((side: Direction) => {
		    if (camoBlock != null) {
			    try {
				    val opRenderer = camoBlock.get(classOf[StaticBlockRenderer])
				    if (opRenderer.isPresent)
					    opRenderer.get.renderSide(side)
			    }
			    catch {
				    case e: Exception =>
					    e.printStackTrace()
			    }
		    }

		    val block = world.getBlock(transform.position + side.toVector)
		    if (block.isPresent) sameType(block.get()) else true
	    }
	    )
		)
		.setTexture(func((dir: Direction) => Optional.of(Textures.forceField)))
	)

	override def getHardness: Double = Double.PositiveInfinity

	override def getResistance: Double = Double.PositiveInfinity

	override def getDrops: util.Set[Item] = Set.empty[Item]

	override def getID: String = "forceField"

	override def weakenForceField(energy: Int) {
		val projector = getProjector

		if (projector != null) {
			projector.addFortron(energy, true)
		}

		if (Game.instance.networkManager.isServer) {
			world.removeBlock(transform.position)
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
			world.removeBlock(transform.position)
		}

		return null
	}

	def getProjectorSafe: BlockProjector = {
		if (projector != null) {

			val projBlock = world.getBlock(projector)
			if (projBlock.isPresent) {
				val proj = projBlock.get().asInstanceOf[BlockProjector]
				if (Game.instance.networkManager.isClient || (proj.getCalculatedField != null && proj.getCalculatedField.contains(transform.position))) {
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
			camoBlock = MFFSUtility.getCamoBlock(getProjector, transform.position).getDummy
		}
	}
}
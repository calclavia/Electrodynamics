package com.calclavia.edx.optics.field

import java.util.{Collections, Optional}

import com.calclavia.edx.core.EDX
import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.optics.api.machine.ForceField
import com.calclavia.edx.optics.api.modules.Module
import com.calclavia.edx.optics.content.{OpticsContent, OpticsTextures}
import com.calclavia.edx.optics.security.MFFSPermissions
import com.calclavia.edx.optics.util.MFFSUtility
import nova.core.block.{Stateful, Block}
import nova.core.block.Block.DropEvent
import nova.core.block.component.{LightEmitter, StaticBlockRenderer}
import nova.core.component.misc.Collider.CollideEvent
import nova.core.component.misc.Damageable
import nova.core.component.renderer.StaticRenderer
import nova.core.component.transform.BlockTransform
import nova.core.entity.Entity
import nova.core.entity.component.Player
import nova.core.network.NetworkTarget.Side
import nova.core.network.{Packet, Sync, Syncable}
import nova.core.render.model.Model
import nova.core.retention.{Storable, Store}
import nova.core.util.Direction
import nova.scala.wrapper.FunctionalWrapper._
import nova.scala.wrapper.VectorWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

class BlockForceField extends BlockEDX with Stateful with Syncable with ForceField with Storable {

	@Store
	@Sync
	private var camoBlock: Block = null
	@Store
	@Sync
	private var projector: Vector3D = null

	/**
	 * Constructor
	 */
	private val superOcclusion = collider.occlusionBoxes

	collider.setOcclusionBoxes(
		(entity: Optional[Entity]) => {
			val projector = getProjector()

			if (projector != null && entity.isPresent && entity.get.getOp(classOf[Player]).isPresent) {
				val biometricIdentifier = projector.getBiometricIdentifier
				val entityPlayer = entity.get.get(classOf[Player])

				if (biometricIdentifier != null) {
					if (biometricIdentifier.hasPermission(entityPlayer.getPlayerID, MFFSPermissions.forceFieldWarp)) {
						null
					}
				}
			}
			//Compose function
			superOcclusion.apply(entity)
		}
	)

	collider.isCube(false)
	collider.isOpaqueCube(false)

	collider.onCollide(
		(evt: CollideEvent) => {
			val projector = getProjector()
			val entity = evt.entity
			if (projector != null) {
				if (projector.crystalHandler.getModules().forall(stack => stack.asInstanceOf[Module].onFieldCollide(BlockForceField.this, entity))) {
					val biometricIdentifier = projector.getBiometricIdentifier

					if ((transform.position + 0.5).distance(entity.transform.position) < 0.5) {
						if (Side.get().isServer && entity.has(classOf[Damageable])) {
							val entityLiving = entity.get(classOf[Damageable])

							if (entity.getOp(classOf[Player]).isPresent) {
								val player = entity.get(classOf[Player])

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
		}
	)

	add(new LightEmitter().setEmittedLevel(supplier(() => {
		val projector = getProjectorSafe
		if (projector != null) {
			Math.min(projector.getModuleCount(OpticsContent.moduleGlow), 64) / 64f
		}
		0f
	})))

	private val renderer = add(new StaticBlockRenderer(this))

	renderer.setRenderSide(
		predicate((side: Direction) => {
			if (camoBlock != null) {
				try {
					val opRenderer = camoBlock.getOp(classOf[StaticBlockRenderer])
					if (opRenderer.isPresent) {
						opRenderer.get.renderSide.test(side)
					}
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

	renderer.setTexture(OpticsTextures.forceField)

	private val superRender = renderer.onRender

	renderer.setOnRender(
		(model: Model) => {
			val opRenderer = if (camoBlock != null) camoBlock.getOp(classOf[StaticRenderer]) else Optional.empty

			if (opRenderer.isPresent) {
				opRenderer.get.onRender.accept(model)
			}
			else {
				superRender.accept(model)
			}
		}
	)

	events.on(classOf[DropEvent]).bind((evt: DropEvent) => evt.drops = Collections.emptySet())

	override def getHardness: Double = Double.PositiveInfinity

	override def getResistance: Double = Double.PositiveInfinity

	override def getID: String = "forceField"

	override def weakenForceField(energy: Int) {
		val projector = getProjector

		//TODO: Disable field
		if (EDX.network.isServer) {
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

		if (EDX.network.isServer) {
			world.removeBlock(transform.position)
		}

		return null
	}

	def getProjectorSafe: BlockProjector = {
		if (projector != null) {
			val projBlock = world.getBlock(projector)
			if (projBlock.isPresent) {
				val proj = projBlock.get().asInstanceOf[BlockProjector]
				if (EDX.network.isClient || (proj.getCalculatedField != null && proj.getCalculatedField.contains(transform.position))) {
					return proj
				}
			}
		}

		return null
	}

	def setProjector(position: Vector3D) {
		projector = position

		if (EDX.network.isServer) {
			refreshCamoBlock()
		}
	}

	def refreshCamoBlock() {
		if (getProjectorSafe != null) {
			//			camoBlock = MFFSUtility.getCamoBlock(getProjector, transform.position).getDummy
		}
	}

	override def read(packet: Packet) {
		if (packet.readBoolean()) {
			//TODO: This is WRONG
			camoBlock = packet.read(classOf[Block])
		}

		if (packet.readBoolean()) {
			projector = packet.readVector3D()
		}
	}

	override def write(packet: Packet) {
		packet.write(camoBlock != null)
		if (camoBlock != null) {
			packet.write(camoBlock)
		}

		packet.write(projector != null)
		if (projector != null) {
			packet.write(projector)
		}
	}
}
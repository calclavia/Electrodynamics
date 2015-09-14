package com.calclavia.edx.optics.field

import java.util.{Collections, Optional}

import com.calclavia.edx.core.EDX
import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.optics.api.machine.ForceField
import com.calclavia.edx.optics.api.modules.Module
import com.calclavia.edx.optics.content.{OpticsContent, OpticsTextures}
import com.calclavia.edx.optics.security.MFFSPermissions
import nova.core.block.Block.DropEvent
import nova.core.block.component.LightEmitter
import nova.core.block.{Block, Stateful}
import nova.core.component.misc.Collider.CollideEvent
import nova.core.component.misc.Damageable
import nova.core.component.renderer.StaticRenderer
import nova.core.entity.Entity
import nova.core.entity.component.Player
import nova.core.network.NetworkTarget.Side
import nova.core.network.{Packet, Sync, Syncable}
import nova.core.render.model.Model
import nova.core.render.pipeline.BlockRenderPipeline
import nova.core.retention.{Storable, Store}
import nova.core.util.Direction
import nova.scala.wrapper.FunctionalWrapper._
import nova.scala.wrapper.VectorWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

class BlockForceField extends BlockEDX with Stateful with Syncable with ForceField with Storable {

	/**
	 * Constructor
	 */
	private val superOcclusion = collider.occlusionBoxes
	private val renderer = components.add(new StaticRenderer())
	private val superRender = renderer.onRender

	collider.setOcclusionBoxes(
		(entity: Optional[Entity]) => {
			val projector = getProjector()

			if (projector != null && entity.isPresent && entity.get.components.has(classOf[Player])) {
				val biometricIdentifier = projector.getBiometricIdentifier
				val entityPlayer = entity.get.components.get(classOf[Player])

				if (biometricIdentifier != null) {
					if (biometricIdentifier.hasPermission(entityPlayer.getPlayerID, MFFSPermissions.forceFieldWarp)) {
						Set.empty
					}
				}
			}
			//Compose function
			superOcclusion.apply(entity)
		}
	)

	collider.isCube(false)
	collider.isOpaqueCube(false)

	events
		.on(classOf[CollideEvent])
		.bind(
	    (evt: CollideEvent) => {
		    val projector = getProjector()
		    val entity = evt.entity
		    if (projector != null) {
			    if (projector.crystalHandler.getModules().forall(stack => stack.asInstanceOf[Module].onFieldCollide(BlockForceField.this, entity))) {
				    val biometricIdentifier = projector.getBiometricIdentifier

				    if ((transform.position + 0.5).distance(entity.transform.position) < 0.5) {
					    if (Side.get().isServer && entity.components.has(classOf[Damageable])) {
						    val entityLiving = entity.components.get(classOf[Damageable])

						    if (entity.components.getOp(classOf[Player]).isPresent) {
							    val player = entity.components.get(classOf[Player])

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

	components.add(new LightEmitter().setEmittedLevel(supplier(() => {
		val projector = getProjectorSafe
		if (projector != null) {
			Math.min(projector.getModuleCount(OpticsContent.moduleGlow), 64) / 64f
		}
		0f
	})))
	@Store
	@Sync
	private var camoBlock: Block = null

	renderer.onRender(
		new BlockRenderPipeline(this)
			.filter(
		    predicate((side: Direction) => {
			    //					if (camoBlock != null) {
			    //						try {
			    //							val opRenderer = camoBlock.getOp(classOf[StaticRenderer])
			    //							if (opRenderer.isPresent) {
			    //								opRenderer.get.renderSide.test(side)
			    //							}
			    //						}
			    //						catch {
			    //							case e: Exception =>
			    //								e.printStackTrace()
			    //						}
			    //					}

			    val block = world.getBlock(transform.position + side.toVector)
			    if (block.isPresent) !sameType(block.get()) else true
		    })
			)
			.withTexture(OpticsTextures.forceField)
			.build()
	)
	@Store
	@Sync
	private var projector: Vector3D = null

	/*	val itemRenderer = add(new ItemRenderer(this))
			.setOnRender((model: Model) => superRender.accept(model))
	*/
	renderer.onRender(
		(model: Model) => {
			val opRenderer = if (camoBlock != null) camoBlock.components.getOp(classOf[StaticRenderer]) else Optional.empty

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
			if (projBlock.isPresent && projBlock.get().isInstanceOf[BlockProjector]) {
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
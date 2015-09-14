package com.calclavia.edx.optics.field

import java.util.{Optional, Random, Set => JSet}

import com.calclavia.edx.core.EDX
import com.calclavia.edx.optics.Settings
import com.calclavia.edx.optics.api.machine.Projector
import com.calclavia.edx.optics.api.modules.Module.ProjectState
import com.calclavia.edx.optics.beam.fx.EntityMagneticBeam
import com.calclavia.edx.optics.component.{BlockFieldMatrix, BlockPacketID, ItemModule}
import com.calclavia.edx.optics.content.{OpticsContent, OpticsModels, OpticsTextures}
import com.calclavia.edx.optics.field.shape.{ItemShape, ItemShapeCustom}
import com.calclavia.edx.optics.fx.FXHologramProgress
import com.calclavia.edx.optics.security.PermissionHandler
import com.calclavia.edx.optics.util.CacheHandler
import nova.core.block.Block.RightClickEvent
import nova.core.block.Stateful.UnloadEvent
import nova.core.block.component.LightEmitter
import nova.core.component.inventory.InventorySimple
import nova.core.component.renderer.{DynamicRenderer, ItemRenderer}
import nova.core.component.transform.Orientation
import nova.core.entity.component.Player
import nova.core.event.bus.EventBus
import nova.core.item.Item
import nova.core.network.{Packet, Sync}
import nova.core.render.Color
import nova.core.render.model.{MeshModel, Model}
import nova.core.retention.Store
import nova.core.util.math.Vector3DUtil
import nova.scala.wrapper.FunctionalWrapper._
import nova.scala.wrapper.VectorWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D

import scala.collection.convert.wrapAll._

class BlockProjector extends BlockFieldMatrix with Projector with PermissionHandler {

	/**
	 * The inventory of the field matrix.
	 *
	 * 1: Shape
	 * 1: Card
	 * 4 * 6: 4 slots per side.
	 */
	@Store
	@Sync(ids = Array(BlockPacketID.description, BlockPacketID.inventory))
	override val inventory = components.add(new InventorySimple(1 + 1 + 4 * 6))
	private val dynamicRenderer = components.add(new DynamicRenderer())
	private val lightEmitter = components.add(new LightEmitter())
	/** A set containing all positions of all force field blocks generated. */
	var forceFields = Set.empty[Vector3D]
	/** Marks the field for an update call */
	var markFieldUpdate = true
	/** True if the field is done constructing and the projector is simply maintaining the field  */
	private var isCompleteConstructing = false
	/** True to make the field constantly tick */
	private var fieldRequireTicks = false
	/** Are the filters in the projector inverted? */
	@Sync(ids = Array(BlockPacketID.description))
	private var isInverted = false

	crystalHandler.capacityBase = 30
	crystalHandler.startModuleIndex = 1

	events.on(classOf[UnloadEvent]).withPriority(EventBus.PRIORITY_DEFAULT + 1).bind((evt: UnloadEvent) => destroyField())

	collider.isCube(false)

	staticRenderer.onRender(
		(model: Model) => {
			model.matrix.rotate(components.get(classOf[Orientation]).orientation.rotation)
			val subModel = OpticsModels.projector.getModel
			model.children.add(subModel)
			subModel.bindAll(if (isActive) OpticsTextures.projectorOn else OpticsTextures.projectorOff)
		}
	)

	dynamicRenderer.onRender(
		(model: Model) => {
			/**
			 * Render the light beam
			 */
			if (getShapeItem != null) {
				/**
				 * Render hologram
				 */
				if (Settings.highGraphics) {
					val hologram = new MeshModel("hologram")
					//GL_SRC_ALPHA
					hologram.blendSFactor = 0x302
					//GL_ONE
					hologram.blendDFactor = 0x303
					hologram.matrix
						.translate(0, 0.85 + Math.sin(Math.toRadians(ticks * 3)).toFloat / 7, 0)
						.rotate(Vector3D.PLUS_J, Math.toRadians(ticks * 4))
						.rotate(new Vector3D(0, 1, 1), Math.toRadians(36f + ticks * 4))

					getShapeItem.components.get(classOf[ItemRenderer]).onRender.accept(hologram)

					val color = if (isActive) Color.blue else Color.red
					hologram.faces.foreach(_.vertices.foreach(_.color = color.alpha((Math.sin(ticks / 10d) * 100 + 155).toInt)))
					model.addChild(hologram)
				}
			}
		}
	)

	lightEmitter.setEmittedLevel(supplier(() => if (getShapeItem() != null) 1f else 0f))

	events.on(classOf[RightClickEvent])
		.bind(
			(evt: RightClickEvent) => {

				if (EDX.network.isServer) {
					val opPlayer = evt.entity.components.getOp(classOf[Player])
					if (opPlayer.isPresent) {
						val player = opPlayer.get()
						val opItem = player.getInventory.getHeldItem

						//The 2D position clicked relative to the face
						val flatPos =
							evt.side match {
								case side if side.toVector.y != 0 =>
									//Evaluating up and down
									new Vector2D(evt.position.x, evt.position.z)
								case side if side.toVector.z != 0 =>
									//Evaluating north and south
									new Vector2D(evt.position.x, evt.position.y)
								case side if side.toVector.x != 0 =>
									//Evaluating east and west
									new Vector2D(evt.position.z, evt.position.y)
							}

						val sideSlot =
							if (Math.abs(flatPos.x) > Math.abs(flatPos.y)) {
								if (flatPos.x > 0) 2 else 4
							}
							else {
								if (flatPos.y > 0) 1 else 3
							}

						if (opItem.isPresent) {
							val item = opItem.get

							def swapSlot(slot: Int) {
								val swap = inventory.swap(slot, item)

								if (swap.isPresent) {
									player.getInventory.set(player.getInventory.getHeldSlot, swap.get)
								} else {
									player.getInventory.remove(player.getInventory.getHeldSlot)
								}
							}

							//Placing shape crystals
							item match {
								case item: ItemShape =>
									swapSlot(0)
								case item: ItemModule =>
									//Prevent center click
									if (flatPos.getNorm > 0.4) {

										val slot = evt.side.ordinal * 4 + sideSlot
										swapSlot(slot)
									}
							}
						}
						else {
							//Eject items. Center click ejects shape crystal
							val rem = inventory.remove(if (flatPos.getNorm > 0.4) sideSlot else 0)
							if (rem.isPresent) {
								player.getInventory.set(player.getInventory.getHeldSlot, rem.get)
							}
						}

						EDX.network.sync(BlockPacketID.inventory, this)
						evt.result = true
					}
				}
			}
		)

	override def start() {
		super.start()
		calculateField(postCalculation)
	}

	override def write(packet: Packet) {
		super.write(packet)
		packet.getID match {
			case BlockPacketID.field =>
			/*
			val nbt = new NBTTagCompound
			val nbtList = new NBTTagList
			calculatedField foreach (vec => nbtList.appendTag(vec.toNBT))
			nbt.setTag("blockList", nbtList)
			ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(this, PacketBlock.field.id: Integer, nbt))*/
			case BlockPacketID.effect =>
				packet <<< transform.position
			case BlockPacketID.effect2 =>
				packet <<< transform.position
			case _ =>
		}
	}

	override def read(packet: Packet) {
		super.read(packet)

		if (EDX.network.isClient) {
			if (packet.getID == BlockPacketID.effect) {
				//Spawns a holographic beam
				val packetType = packet.readInt
				val target = new Vector3D(packet.readInt, packet.readInt, packet.readInt) + 0.5
				val pos = transform.position + 0.5

				if (packetType == 1) {
					world.addClientEntity(OpticsContent.fxFortronBeam).transform.setPosition(pos).asInstanceOf[EntityMagneticBeam].setTarget(target)
					world.addClientEntity(OpticsContent.fxHologramProgress).transform.setPosition(pos)
				}
				else if (packetType == 2) {
					world.addClientEntity(OpticsContent.fxFortronBeam).transform.setPosition(pos).asInstanceOf[EntityMagneticBeam].setTarget(target).color = (Color.red)
					world.addClientEntity(OpticsContent.fxHologramProgress).transform.setPosition(pos).asInstanceOf[FXHologramProgress].color = (Color.red)
				}
			}
			else if (packet.getID == BlockPacketID.field) {
				//Receives the entire force field
				//				val nbt = PacketUtils.readTag(packet)
				//				val nbtList = nbt.getTagList("blockList", 10)
				//				calculatedField = mutable.Set(((0 until nbtList.tagCount) map (i => new Vector3d(nbtList.getCompoundTagAt(i)))).toArray: _ *)
			}
		}
		else {
			if (packet.getID == BlockPacketID.toggleMode2) {
				isInverted = !isInverted
			}
		}
	}

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		if (EDX.network.isServer) {
			//println(getShapeItem + " : " +opticHandler.power + " vs "+crystalHandler.powerCost)
			if (getShapeItem != null && opticHandler.power > crystalHandler.powerCost) {
				setActive(true)
			}
			else {
				setActive(false)
			}
		}

		if (isActive) {
			if (ticks % 10 == 0 || markFieldUpdate || fieldRequireTicks) {
				if (calculatedField == null) {
					calculateField(postCalculation)
				}
				else {
					projectField()
				}
			}

			if (EDX.network.isClient) {
				animation += crystalHandler.powerCost / 100f
			}

			if (ticks % (2 * 20) == 0 && getModuleCount(OpticsContent.moduleSilence) <= 0) {
				world.playSoundAtPosition(position + (Vector3DUtil.ONE * 0.5), OpticsContent.soundField.withVolume(0.6f).withPitch(1 - new Random().nextFloat * 0.1f))
			}
		}
		else if (EDX.network.isServer) {
			destroyField()
		}
	}

	def postCalculation() = if (clientSideSimulationRequired) EDX.network.sync(BlockPacketID.field, this)

	private def clientSideSimulationRequired: Boolean = getModuleCount(OpticsContent.moduleRepulsion) > 0

	/**
	 * Initiate a field calculation
	 */
	protected override def calculateField(callBack: () => Unit = null) {
		if (EDX.network.isServer && !isCalculating) {
			if (getShapeItem != null) {
				forceFields = Set.empty
			}

			super.calculateField(callBack)
			isCompleteConstructing = false
			fieldRequireTicks = crystalHandler.getModules().exists(_.requireTicks)
		}
	}

	/**
	 * Projects a force field based on the calculations made.
	 */
	def projectField() {
		//TODO: We cannot construct a field if it intersects another field with different frequency.
		if (!isCalculating) {
			val potentialField = calculatedField
			val relevantModules = crystalHandler.getModules(getModuleSlots: _*)

			if (!relevantModules.exists(_.onCreateField(this, potentialField))) {
				if (!isCompleteConstructing || markFieldUpdate || fieldRequireTicks) {
					markFieldUpdate = false

					if (forceFields.size <= 0) {
						if (getShapeItem.isInstanceOf[CacheHandler]) {
							getShapeItem.asInstanceOf[CacheHandler].clearCache
						}
					}

					val constructionSpeed = Math.min(getProjectionSpeed, Settings.maxForceFieldsPerTick)

					//Creates a collection of positions that will be evaluated
					val evaluateField = potentialField
						.view.par
						.filter(!_.equals(transform.position))
						.filter(v => world.getBlock(v).isPresent && world.getBlock(v).get().canReplace)
						//.filter(v => world.getBlock(v).get().sameType(OpticsContent.forceField))
						.take(constructionSpeed)

					//The collection containing the coordinates to actually place the field blocks.
					var constructField = Set.empty[Vector3D]

					val result = evaluateField.forall(
						vector => {
							var flag = ProjectState.pass

							for (module <- relevantModules) {
								if (flag == ProjectState.pass) {
									flag = module.onProject(this, vector)
								}
							}

							if (flag == ProjectState.pass) {
								constructField += vector
							}

							flag != ProjectState.cancel
						})

					if (result) {
						constructField.foreach(
							pos => {
								/**
								 * Default force field block placement action.
								 */
								if (EDX.network.isServer) {
									world.setBlock(pos, OpticsContent.forceField)
									world.getBlock(pos).get().asInstanceOf[BlockForceField].setProjector(position)
								}

								forceFields += pos
							})
					}

					isCompleteConstructing = evaluateField.isEmpty
				}
			}
		}
	}

	def getProjectionSpeed: Int = 28 + 28 * getModuleCount(OpticsContent.moduleSpeed, getModuleSlots: _*)

	def destroyField() {
		if (EDX.network.isServer && calculatedField != null && !isCalculating) {
			println("Destroyed field")
			crystalHandler.getModules(getModuleSlots: _*).forall(!_.onDestroyField(this, calculatedField))
			//TODO: Parallelism?
			calculatedField
				.view
				.filter(v => world.getBlock(v).isPresent && world.getBlock(v).get().sameType(OpticsContent.forceField))
				.foreach(v => world.removeBlock(v))

			forceFields = Set.empty
			calculatedField = null
			isCompleteConstructing = false
			fieldRequireTicks = false
		}
	}

	override def getForceFields: JSet[Vector3D] = forceFields

	override def isInField(position: Vector3D) = if (getShapeItem != null) getStructure.intersects(position) else false

	/*
	def isAccessGranted(checkWorld: World, checkPos: Vector3d, player: EntityPlayer, action: PlayerInteractEvent.Action): Boolean = {
		var hasPerm = true

		if (action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && checkPos.getTileEntity(checkWorld) != null) {
			if (getModuleCount(Content.moduleBlockAccess) > 0) {
				hasPerm = hasPermission(player.getGameProfile, MFFSPermissions.world)
			}
		}

		if (hasPerm) {
			if (getModuleCount(Content.moduleBlockAlter) > 0 && (player.getCurrentEquippedItem != null || action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)) {
				hasPerm = hasPermission(player.getGameProfile, MFFSPermissions.blockAlter)
			}
		}

		return hasPerm
	}*/

	def getFilterItems: Set[Item] = (26 until 32).map(inventory.get).collect { case op: Optional[Item] if op.isPresent => op.get }.toSet

	def isInvertedFilter: Boolean = isInverted

	//TODO: Useless
	protected def amplifier: Float = {
		if (getShapeItem.isInstanceOf[ItemShapeCustom]) {
			return Math.max(getShapeItem.asInstanceOf[ItemShapeCustom].fieldSize / 100, 1)
		}
		return Math.max(Math.min((if (calculatedField != null) calculatedField.size else 0) / 1000, 10), 1)
	}
}
package mffs.field

import java.util.{Set => JSet}

import mffs.api.machine.Projector
import mffs.base.{BlockFieldMatrix, PacketBlock}
import mffs.content.Content
import mffs.field.mode.ItemModeCustom
import mffs.render.FieldColor
import mffs.security.MFFSPermissions
import mffs.util.CacheHandler
import mffs.{ModularForceFieldSystem, Settings}
import nova.core.block.components.LightEmitter
import nova.core.game.Game
import nova.core.inventory.InventorySimple
import nova.core.item.Item
import nova.core.network.{Packet, Sync}
import nova.core.util.transform.{Cuboid, Vector3i}

class BlockProjector extends BlockFieldMatrix with Projector with LightEmitter {

	/** A set containing all positions of all force field blocks generated. */
	var forceFields = Set.empty[Vector3i]

	/** Marks the field for an update call */
	var markFieldUpdate = true

	/** True if the field is done constructing and the projector is simply maintaining the field  */
	private var isCompleteConstructing = false

	/** True to make the field constantly tick */
	private var fieldRequireTicks = false

	/** Are the filters in the projector inverted? */
	@Sync(ids = Array(PacketBlock.description.ordinal()))
	private var isInverted = false

	capacityBase = 30
	startModuleIndex = 1

	override protected val inventory = new InventorySimple(1 + 25 + 6)

	override def getBoundingBox: Cuboid = new Cuboid(0, 0, 0, 1, 0.8, 1)

	/*
	override def isItemValidForSlot(slotID: Int, Item: Item): Boolean = {
		slotID match {
			case 0 => Item.getItem.isInstanceOf[ItemCard]
			case `modeSlotID` => Item.getItem.isInstanceOf[IProjectorMode]
			case x: Int if x < 26 => Item.getItem.isInstanceOf[IModule]
			case _ => true
		}
	}*/

	override def start() {
		super.start()
		calculateField(postCalculation)
	}

	override def getEmittedLightLevel: Float = if (getShape() != null) 1 else 0

	override def write(packet: Packet) {
		super.write(packet)
		if (packet.getID == PacketBlock.field.ordinal()) {
			/*
			val nbt = new NBTTagCompound
			val nbtList = new NBTTagList
			calculatedField foreach (vec => nbtList.appendTag(vec.toNBT))
			nbt.setTag("blockList", nbtList)
			ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(this, PacketBlock.field.id: Integer, nbt))*/
		}
	}

	override def read(id: Int, packet: Packet) {
		super.read(id, packet)

		if (Game.instance.networkManager.isClient) {
			if (id == PacketBlock.effect.ordinal()) {
				//Spawns a holographic beam
				val packetType = packet.readInt
				val vector = new Vector3i(packet.readInt, packet.readInt, packet.readInt) + 0.5
				val root = position.toDouble + 0.5

				if (packetType == 1) {
					ModularForceFieldSystem.proxy.renderBeam(this.worldObj, root, vector, FieldColor.blue, 40)
					ModularForceFieldSystem.proxy.renderHologramMoving(this.worldObj, vector, FieldColor.blue, 50)
				}
				else if (packetType == 2) {
					ModularForceFieldSystem.proxy.renderBeam(this.worldObj, vector, root, FieldColor.red, 40)
					ModularForceFieldSystem.proxy.renderHologramMoving(this.worldObj, vector, FieldColor.red, 50)
				}
			}
			else if (id == PacketBlock.field.ordinal()) {
				//Receives the entire force field
				//				val nbt = PacketUtils.readTag(packet)
				//				val nbtList = nbt.getTagList("blockList", 10)
				//				calculatedField = mutable.Set(((0 until nbtList.tagCount) map (i => new Vector3d(nbtList.getCompoundTagAt(i)))).toArray: _ *)
			}
		}
		else {
			if (id == PacketBlock.toggleMode2.ordinal()) {
				isInverted = !isInverted
			}
		}
	}

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		if (isActive && getShape != null && removeFortron(getFortronCost, false) >= this.getFortronCost) {
			consumeCost()

			if (ticks % 10 == 0 || markFieldUpdate || fieldRequireTicks) {
				if (calculatedField == null) {
					calculateField(postCalculation)
				}
				else {
					projectField()
				}
			}

			if (isActive && Game.instance.networkManager.isClient) {
				animation += getFortronCost / 100f
			}

			if (ticks % (2 * 20) == 0 && getModuleCount(Content.moduleSilence) <= 0) {
				//TODO: Fix world sound effects
				//worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, Reference.prefix + "field", 0.6f, (1 - this.worldObj.rand.nextFloat * 0.1f))
			}
		}
		else if (Game.instance.networkManager.isServer) {
			destroyField()
		}
	}

	def postCalculation() = if (clientSideSimulationRequired) Game.instance.networkManager.sync(PacketBlock.field.ordinal(), this)

	private def clientSideSimulationRequired: Boolean = {
		return getModuleCount(Content.moduleRepulsion) > 0
	}

	/**
	 * Initiate a field calculation
	 */
	protected override def calculateField(callBack: () => Unit = null) {
		if (Game.instance.networkManager.isServer && !isCalculating) {
			if (getShape != null) {
				forceFields = Set.empty
			}

			super.calculateField(callBack)
			isCompleteConstructing = false
			fieldRequireTicks = getModules().exists(_.requireTicks)
		}
	}

	/**
	 * Projects a force field based on the calculations made.
	 */
	def projectField() {
		//TODO: We cannot construct a field if it intersects another field with different frequency.
		if (!isCalculating) {
			val potentialField = calculatedField
			val relevantModules = getModules(getModuleSlots: _*)

			if (!relevantModules.exists(_.onCreateField(this, potentialField))) {
				if (!isCompleteConstructing || markFieldUpdate || fieldRequireTicks) {
					markFieldUpdate = false

					if (forceFields.size <= 0) {
						if (getShape.isInstanceOf[CacheHandler]) {
							getShape.asInstanceOf[CacheHandler].clearCache
						}
					}

					val constructionSpeed = Math.min(getProjectionSpeed, Settings.maxForceFieldsPerTick)

					//Creates a collection of positions that will be evaluated
					val evaluateField = potentialField
						.view.par
						.filter(!_.equals(position))
						.filter(v => canReplaceBlock(v, world.getBlock(v)))
						.filter(v => world.getBlock(v).isPresent && world.getBlock(v).get().sameType(Content.forceField))
						.take(constructionSpeed)

					//The collection containing the coordinates to actually place the field blocks.
					var constructField = Set.empty[Vector3i]

					val result = evaluateField.forall(
						vector => {
							var flag = 0

							for (module <- relevantModules) {
								if (flag == 0) {
									flag = module.onProject(this, vector)
								}
							}

							if (flag != 1 && flag != 2) {
								constructField += vector
							}

							flag != 2
						})

					if (result) {
						constructField.foreach(
							pos => {
								/**
								 * Default force field block placement action.
								 */
								if (Game.instance.networkManager.isServer) {
									world.setBlock(pos, Content.forceField)
								}

								forceFields += pos

								val tileEntity = pos.getTileEntity(world)

								if (tileEntity.isInstanceOf[BlockForceField]) {
									tileEntity.asInstanceOf[BlockForceField].setProjector(position)
								}
							})
					}

					isCompleteConstructing = evaluateField.size == 0
				}
			}
		}
	}

	private def canReplaceBlock(vector: Vector3d, block: Block): Boolean = {
		return block == null ||
			(getModuleCount(Content.moduleDisintegration) > 0 && block.getBlockHardness(this.worldObj, vector.xi, vector.yi, vector.zi) != -1) ||
			(block.getMaterial.isLiquid || block == Blocks.snow || block == Blocks.vine || block == Blocks.tallgrass || block == Blocks.deadbush || block.isReplaceable(world, vector.xi, vector.yi, vector.zi))
	}

	def getProjectionSpeed: Int = 28 + 28 * getModuleCount(Content.moduleSpeed, getModuleSlots: _*)

	def destroyField() {
		if (Game.instance.networkManager.isServer && calculatedField != null && !isCalculating) {
			getModules(getModuleSlots: _*).forall(!_.onDestroy(this, calculatedField))
			//TODO: Parallelism?
			calculatedField.view filter (_.getBlock(world) == Content.forceField) foreach (_.setBlock(world, Blocks.air))

			forceFields.clear()
			calculatedField = null
			isCompleteConstructing = false
			fieldRequireTicks = false
		}
	}

	override def markDirty() {
		super.markDirty()

		if (world != null) {
			destroyField()
		}
	}

	override def invalidate {
		destroyField()
		super.invalidate
	}

	override def getForceFields: JSet[Vector3d] = forceFields

	def getTicks: Long = ticks

	def isInField(position: Vector3d) = if (getShape != null) getShape.isInField(this, position) else false

	def isAccessGranted(checkWorld: World, checkPos: Vector3d, player: EntityPlayer, action: PlayerInteractEvent.Action): Boolean = {
		var hasPerm = true

		if (action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && checkPos.getTileEntity(checkWorld) != null) {
			if (getModuleCount(Content.moduleBlockAccess) > 0) {
				hasPerm = hasPermission(player.getGameProfile, MFFSPermissions.blockAccess)
			}
		}

		if (hasPerm) {
			if (getModuleCount(Content.moduleBlockAlter) > 0 && (player.getCurrentEquippedItem != null || action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)) {
				hasPerm = hasPermission(player.getGameProfile, MFFSPermissions.blockAlter)
			}
		}

		return hasPerm
	}

	def getFilterItems: Set[Item] = getFilterStacks map (_.getItem)

	def getFilterStacks: Set[Item] = ((26 until 32) map (getStackInSlot(_)) filter (_ != null)).toSet

	def isInvertedFilter: Boolean = isInverted

	/**
	 * Rendering
	 */
	@SideOnly(Side.CLIENT)
	override def renderStatic(renderer: RenderBlocks, pos: Vector3d, pass: Int): Boolean = {
		return false
	}

	@SideOnly(Side.CLIENT)
	override def renderDynamic(pos: Vector3d, frame: Float, pass: Int) {
		RenderElectromagneticProjector.render(this, pos.x, pos.y, pos.z, frame, isActive, false)
	}

	@SideOnly(Side.CLIENT)
	override def renderInventory(Item: Item) {
		RenderElectromagneticProjector.render(this, -0.5, -0.5, -0.5, 0, true, true)
	}

	/**
	 * Returns Fortron cost in ticks.
	 */
	protected override def doGetFortronCost: Int = {
		if (this.getShape != null) {
			return Math.round(super.doGetFortronCost + this.getShape.getFortronCost(this.getAmplifier))
		}
		return 0
	}

	protected override def getAmplifier: Float = {
		if (this.getShape.isInstanceOf[ItemModeCustom]) {
			return Math.max((this.getShape.asInstanceOf[ItemModeCustom]).getFieldBlocks(this, this.getShape).size / 100, 1)
		}
		return Math.max(Math.min((if (calculatedField != null) calculatedField.size else 0) / 1000, 10), 1)
	}
}
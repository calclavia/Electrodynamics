package mffs.field

import java.util.{Optional, Set => JSet}

import mffs.api.machine.Projector
import mffs.base.{BlockFieldMatrix, PacketBlock}
import mffs.content.{Content, Models, Textures}
import mffs.render.FieldColor
import mffs.util.CacheHandler
import mffs.{ModularForceFieldSystem, Settings}
import nova.core.block.Block
import nova.core.block.components.LightEmitter
import nova.core.game.Game
import nova.core.inventory.InventorySimple
import nova.core.item.Item
import nova.core.network.{Packet, Sync}
import nova.core.render.Color
import nova.core.render.model.{Model, Vertex}
import nova.core.util.transform.{Cuboid, MatrixStack, Vector3d, Vector3i}

import scala.collection.convert.wrapAll._

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

	override def getEmittedLightLevel: Float = if (getShapeItem() != null) 1 else 0

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

		if (isActive && getShapeItem != null && removeFortron(getFortronCost, false) >= this.getFortronCost) {
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
			if (getShapeItem != null) {
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
						if (getShapeItem.isInstanceOf[CacheHandler]) {
							getShapeItem.asInstanceOf[CacheHandler].clearCache
						}
					}

					val constructionSpeed = Math.min(getProjectionSpeed, Settings.maxForceFieldsPerTick)

					//Creates a collection of positions that will be evaluated
					val evaluateField = potentialField
						.view.par
						.filter(!_.equals(position))
						.filter(v => !world.getBlock(v).isPresent || canReplaceBlock(v, world.getBlock(v).get()))
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
									world.getBlock(pos).get().asInstanceOf[BlockForceField].setProjector(position)
								}

								forceFields += pos
							})
					}

					isCompleteConstructing = evaluateField.size == 0
				}
			}
		}
	}

	private def canReplaceBlock(vector: Vector3i, block: Block): Boolean = {
		/*	return block == null ||
				(getModuleCount(Content.moduleDisintegration) > 0 && block.getBlockHardness(this.worldObj, vector.xi, vector.yi, vector.zi) != -1) ||
				(block.getMaterial.isLiquid || block == Blocks.snow || block == Blocks.vine || block == Blocks.tallgrass || block == Blocks.deadbush || block.isReplaceable(world, vector.xi, vector.yi, vector.zi))
	*/
		return true
	}

	def getProjectionSpeed: Int = 28 + 28 * getModuleCount(Content.moduleSpeed, getModuleSlots: _*)

	def destroyField() {
		if (Game.instance.networkManager.isServer && calculatedField != null && !isCalculating) {
			getModules(getModuleSlots: _*).forall(!_.onDestroy(this, calculatedField))
			//TODO: Parallelism?
			calculatedField
				.view
				.filter(v => world.getBlock(v).isPresent && world.getBlock(v).get().sameType(Content.forceField))
				.foreach(v => world.setBlock(v, Blocks.air))

			forceFields = Set.empty
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

	override def unload() {
		destroyField()
		super.unload()
	}

	override def getForceFields: JSet[Vector3i] = forceFields

	def getTicks: Long = ticks

	def isInField(position: Vector3d) = if (getShapeItem != null) getStructure.intersects(position) else false

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

	def getFilterItems: Set[Item] = (26 until 32).map(inventory.get).collect { case op: Optional[Item] if op.isPresent => op.get}.toSet

	def isInvertedFilter: Boolean = isInverted

	/**
	 * Rendering
	 */
	override def renderDynamic(model: Model) {
		/**
		 * Rends the model 
		 */
		model.matrix = new MatrixStack()
			.loadMatrix(model.matrix)
			.translate(0, 0.15, 0)
			.scale(1.3, 1.3, 1.3)
			.rotate(direction.rotation)
			.getMatrix

		model.children.add(Models.projector.getModel)

		if (isActive) {
			model.bindAll(Textures.projectorOn)
		}
		else {
			model.bindAll(Textures.projectorOff)
		}

		/**
		 * Render the light beam 
		 */
		if (getShapeItem != null) {
			val lightBeam = new Model()
			//TODO: Lighting, RenderHelper.disableStandardItemLighting

			val xDifference: Double = Minecraft.getMinecraft.thePlayer.posX - (x + 0.5)
			val zDifference: Double = Minecraft.getMinecraft.thePlayer.posZ - (y + 0.5)
			val rotatation = Math.atan2(zDifference, xDifference)
			lightBeam.matrix = new MatrixStack().rotate(Vector3d.yAxis, -rotatation + Math.toRadians(27)).getMatrix
			/*
			glDisable(GL_TEXTURE_2D)
			glShadeModel(GL_SMOOTH)
			glEnable(GL_BLEND)
			glBlendFunc(GL_SRC_ALPHA, GL_ONE)
			glDisable(GL_ALPHA_TEST)
			glEnable(GL_CULL_FACE)
			glDepthMask(false)
			glPushMatrix*/

			val height: Float = 2
			val width: Float = 2

			val face = lightBeam.createFace()
			//tessellator.setColorRGBA_I(0, 0)
			face.drawVertex(new Vertex(0, 0, 0, 0, 0))
			face.drawVertex(new Vertex(-0.866D * width, height, -0.5F * width, 0, 0))
			face.drawVertex(new Vertex(0.866D * width, height, -0.5F * width, 0, 0))
			face.drawVertex(new Vertex(0.0D, height, 1.0F * width, 0, 0))
			face.drawVertex(new Vertex(-0.866D * width, height, -0.5F * width, 0, 0))
			face.vertices.foreach(_.setColor(new Color(72, 198, 255, 255)))
			lightBeam.drawFace(face)

			/*
			glPopMatrix
			glDepthMask(true)
			glDisable(GL_CULL_FACE)
			glDisable(GL_BLEND)
			glShadeModel(GL_FLAT)
			glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
			glEnable(GL_TEXTURE_2D)
			glEnable(GL_ALPHA_TEST)
			RenderHelper.enableStandardItemLighting
			glPopMatrix*/

			model.children.add(lightBeam)

			/**
			 * Render hologram
			 */
			if (Settings.highGraphics) {

				val hologram = new Model()
				hologram.matrix = new MatrixStack()
					.translate(0, 0.85 + Math.sin(Math.toRadians(ticks * 3)).toFloat / 7, 0)
					.rotate(Vector3d.yAxis, Math.toRadians(ticks * 4))
					.rotate(new Vector3d(0, 1, 1), Math.toRadians(36f + ticks * 4))
					.getMatrix

				getShapeItem.render(this, model)

				val color = if (isActive) FieldColor.blue else FieldColor.red
				val rgba = new Color(color._1, color._2, color._3, Math.sin(ticks.toDouble / 10) / 2 + 0.8)
				hologram.faces.foreach(_.vertices.foreach(_.setColor(rgba)))
				hologram.bind(Textures.hologram)

			}
		}
	}

	override def renderStatic(model: Model) {

	}

	override def renderItem(model: Model) = renderDynamic(model)

	/**
	 * Returns Fortron cost in ticks.
	 */
	protected override def doGetFortronCost: Int = if (this.getShapeItem != null) Math.round(super.doGetFortronCost + this.getShapeItem.getFortronCost(this.getAmplifier)) else 0

	protected override def getAmplifier: Float = {
		if (getShapeItem.isInstanceOf[ItemModeCustom]) {
			return Math.max(getShapeItem.asInstanceOf[ItemModeCustom].getFieldBlocks(this, this.getShapeItem).size / 100, 1)
		}
		return Math.max(Math.min((if (calculatedField != null) calculatedField.size else 0) / 1000, 10), 1)
	}
}
package mffs.field

import java.util.{Set => JSet}

import mffs.base.{PacketBlock, TileFieldMatrix}
import mffs.field.mode.ItemModeCustom
import mffs.item.card.ItemCard
import mffs.render.FieldColor
import mffs.security.MFFSPermissions
import mffs.util.TCache
import mffs.{Content, ModularForceFieldSystem, Reference, Settings}

class BlockProjector extends TileFieldMatrix with IProjector
{
  /** A set containing all positions of all force field blocks generated. */
  val forceFields = mutable.Set.empty[Vector3d]

  /** Marks the field for an update call */
  var markFieldUpdate = true

  /** True if the field is done constructing and the projector is simply maintaining the field  */
  private var isCompleteConstructing = false

  /** True to make the field constantly tick */
  private var fieldRequireTicks = false

  /** Are the filters in the projector inverted? */
  private var isInverted = false

  bounds = new Cuboid(0, 0, 0, 1, 0.8, 1)
  capacityBase = 30
  startModuleIndex = 1

  override def getSizeInventory = 1 + 25 + 6

	override def isItemValidForSlot(slotID: Int, Item: Item): Boolean =
  {
    slotID match
    {
		case 0 => Item.getItem.isInstanceOf[ItemCard]
		case `modeSlotID` => Item.getItem.isInstanceOf[IProjectorMode]
		case x: Int if x < 26 => Item.getItem.isInstanceOf[IModule]
      case _ => true
    }
  }

  override def start()
  {
    super.start()
    calculateField(postCalculation)
  }

  override def getLightValue(access: IBlockAccess) = if (getMode() != null) 10 else 0

	override def write(buf: Packet, id: Int)
  {
    super.write(buf, id)

	  if (id == PacketBlock.description.id)
    {
      buf <<< isInverted
    }
  }

	override def read(buf: Packet, id: Int, packetType: PacketType)
  {
    super.read(buf, id, packetType)

    if (worldObj.isRemote)
    {
		if (id == PacketBlock.description.id)
      {
        isInverted = buf.readBoolean()
      }
		else if (id == PacketBlock.effect.id)
      {
        val packetType = buf.readInt
		  val vector = new Vector3d(buf.readInt, buf.readInt, buf.readInt) + 0.5
        val root = position + 0.5

        if (packetType == 1)
        {
          ModularForceFieldSystem.proxy.renderBeam(this.worldObj, root, vector, FieldColor.blue, 40)
          ModularForceFieldSystem.proxy.renderHologramMoving(this.worldObj, vector, FieldColor.blue, 50)
        }
        else if (packetType == 2)
        {
          ModularForceFieldSystem.proxy.renderBeam(this.worldObj, vector, root, FieldColor.red, 40)
          ModularForceFieldSystem.proxy.renderHologramMoving(this.worldObj, vector, FieldColor.red, 50)
        }
      }
		else if (id == PacketBlock.field.id)
      {
		  val nbt = PacketUtils.readTag(buf)
        val nbtList = nbt.getTagList("blockList", 10)
		  calculatedField = mutable.Set(((0 until nbtList.tagCount) map (i => new Vector3d(nbtList.getCompoundTagAt(i)))).toArray: _ *)
      }
    }
    else
    {
		if (id == PacketBlock.toggleMode2.id)
      {
        isInverted = !isInverted
      }
    }
  }

  override def update()
  {
    super.update()

	  if (isActive && getMode != null && addFortron(getFortronCost, false) >= this.getFortronCost)
    {
      consumeCost()

      if (ticks % 10 == 0 || markFieldUpdate || fieldRequireTicks)
      {
        if (calculatedField == null)
        {
          calculateField(postCalculation)
        }
        else
        {
          projectField()
        }
      }

      if (isActive && worldObj.isRemote)
      {
        animation += getFortronCost / 100f
      }
      if (ticks % (2 * 20) == 0 && getModuleCount(Content.moduleSilence) <= 0)
      {
        worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, Reference.prefix + "field", 0.6f, (1 - this.worldObj.rand.nextFloat * 0.1f))
      }
    }
	else if (Game.instance.networkManager.isServer)
    {
      destroyField()
    }
  }

  def postCalculation() = if (clientSideSimulationRequired) sendFieldToClient

  def sendFieldToClient
  {
    val nbt = new NBTTagCompound
    val nbtList = new NBTTagList
    calculatedField foreach (vec => nbtList.appendTag(vec.toNBT))
    nbt.setTag("blockList", nbtList)
	  ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(this, PacketBlock.field.id: Integer, nbt))
  }

  private def clientSideSimulationRequired: Boolean =
  {
    return getModuleCount(Content.moduleRepulsion) > 0
  }

  /**
   * Initiate a field calculation
   */
  protected override def calculateField(callBack: () => Unit = null)
  {
	  if (Game.instance.networkManager.isServer && !isCalculating)
    {
      if (getMode != null)
      {
        forceFields.clear
      }

      super.calculateField(callBack)
      isCompleteConstructing = false
      fieldRequireTicks = getModuleStacks() exists (module => module.getItem.asInstanceOf[IModule].requireTicks(module))
    }
  }

  /**
   * Projects a force field based on the calculations made.
   */
  def projectField()
  {
    //TODO: We cannot construct a field if it intersects another field with different frequency. Override not allowed.

    if (!isCalculating)
    {
      val potentialField = calculatedField

      val relevantModules = getModules(getModuleSlots: _*)

      if (!relevantModules.exists(_.onProject(this, potentialField)))
      {
        if (!isCompleteConstructing || markFieldUpdate || fieldRequireTicks)
        {
          markFieldUpdate = false

          if (forceFields.size <= 0)
          {
            if (getModeStack.getItem.isInstanceOf[TCache])
            {
              (getModeStack.getItem.asInstanceOf[TCache]).clearCache
            }
          }

          val constructionSpeed = Math.min(getProjectionSpeed, Settings.maxForceFieldsPerTick)

          //Creates a collection of positions that will be evaluated
          val evaluateField = potentialField
            .view.par
            .filter(!_.equals(position))
            .filter(v => canReplaceBlock(v, v.getBlock(world)))
            .filter(_.getBlock(world) != Content.forceField)
            .filter(v => world.getChunkFromBlockCoords(v.xi, v.zi).isChunkLoaded)
            .take(constructionSpeed)

          //The collection containing the coordinates to actually place the field blocks.
		  var constructField = Set.empty[Vector3d]

          val result = evaluateField.forall(
            vector =>
            {
              var flag = 0

              for (module <- relevantModules)
              {
                if (flag == 0)
                  flag = module.onProject(this, vector)
              }

              if (flag != 1 && flag != 2)
              {
                constructField += vector
              }

              flag != 2
            })

          if (result)
          {
            constructField.foreach(
              vector =>
              {
                /**
                 * Default force field block placement action.
                 */
				  if (Game.instance.networkManager.isServer) {
					  vector.setBlock(world, Content.forceField)
				  }

                forceFields += vector

                val tileEntity = vector.getTileEntity(world)

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

	private def canReplaceBlock(vector: Vector3d, block: Block): Boolean =
  {
    return block == null ||
           (getModuleCount(Content.moduleDisintegration) > 0 && block.getBlockHardness(this.worldObj, vector.xi, vector.yi, vector.zi) != -1) ||
           (block.getMaterial.isLiquid || block == Blocks.snow || block == Blocks.vine || block == Blocks.tallgrass || block == Blocks.deadbush || block.isReplaceable(world, vector.xi, vector.yi, vector.zi))
  }

  def getProjectionSpeed: Int = 28 + 28 * getModuleCount(Content.moduleSpeed, getModuleSlots: _*)

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

  def destroyField()
  {
	  if (Game.instance.networkManager.isServer && calculatedField != null && !isCalculating)
    {
      getModules(getModuleSlots: _*).forall(!_.onDestroy(this, calculatedField))
      //TODO: Parallelism?
      calculatedField.view filter (_.getBlock(world) == Content.forceField) foreach (_.setBlock(world, Blocks.air))

      forceFields.clear()
      calculatedField = null
      isCompleteConstructing = false
      fieldRequireTicks = false
    }
  }

	override def getForceFields: JSet[Vector3d] = forceFields

  def getTicks: Long = ticks

	def isInField(position: Vector3d) = if (getMode != null) getMode.isInField(this, position) else false

	def isAccessGranted(checkWorld: World, checkPos: Vector3d, player: EntityPlayer, action: PlayerInteractEvent.Action): Boolean =
  {
    var hasPerm = true

    if (action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && checkPos.getTileEntity(checkWorld) != null)
    {
      if (getModuleCount(Content.moduleBlockAccess) > 0)
      {
        hasPerm = hasPermission(player.getGameProfile, MFFSPermissions.blockAccess)
      }
    }

    if (hasPerm)
    {
      if (getModuleCount(Content.moduleBlockAlter) > 0 && (player.getCurrentEquippedItem != null || action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK))
      {
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
  override def renderStatic(renderer: RenderBlocks, pos: Vector3d, pass: Int): Boolean =
  {
    return false
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3d, frame: Float, pass: Int)
  {
    RenderElectromagneticProjector.render(this, pos.x, pos.y, pos.z, frame, isActive, false)
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(Item: Item)
  {
    RenderElectromagneticProjector.render(this, -0.5, -0.5, -0.5, 0, true, true)
  }

  /**
   * Returns Fortron cost in ticks.
   */
  protected override def doGetFortronCost: Int =
  {
    if (this.getMode != null)
    {
      return Math.round(super.doGetFortronCost + this.getMode.getFortronCost(this.getAmplifier))
    }
    return 0
  }

  protected override def getAmplifier: Float =
  {
    if (this.getMode.isInstanceOf[ItemModeCustom])
    {
      return Math.max((this.getMode.asInstanceOf[ItemModeCustom]).getFieldBlocks(this, this.getModeStack).size / 100, 1)
    }
    return Math.max(Math.min((if (calculatedField != null) calculatedField.size else 0) / 1000, 10), 1)
  }
}
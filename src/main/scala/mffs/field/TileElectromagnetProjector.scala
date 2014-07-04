package mffs.field

import java.util.{HashSet, Iterator, Set}

import com.google.common.io.ByteArrayDataInput
import com.mojang.authlib.GameProfile
import cpw.mods.fml.relauncher.{Side, SideOnly}
import mffs.base.TileFieldInteraction
import mffs.field.mode.ItemModeCustom
import mffs.item.card.ItemCard
import mffs.security.access.MFFSPermissions
import mffs.{TCache, ModularForceFieldSystem, Settings}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import resonant.api.mffs.modules.{IModule, IProjectorMode}
import resonant.api.mffs.IProjector
import resonant.lib.access.Permission
import universalelectricity.core.transform.region.Cuboid
import universalelectricity.core.transform.vector.Vector3

class TileElectromagnetProjector extends TileFieldInteraction with IProjector
{
  /** A set containing all positions of all force field blocks generated. */
  val forceFields = Set[Vector3]

  /** Marks the field for an update call */
  var markFieldUpdate = true

  /** True if the field is done constructing and the projector is simply maintaining the field  */
  private var isCompleteConstructing = false

  /** True to make the field constantly tick */
  private var fieldRequireTicks = false

  bounds = new Cuboid(0, 0, 0, 1, 0.8, 1)
  capacityBase = 50
  startModuleIndex = 1

  override def start()
  {
    super.start()
    this.calculateForceField(this)
  }

  override def getLightValue(access: IBlockAccess) = if (getMode() != null) 10 else 0

  override def onReceivePacket(packetID: Int, dataStream: ByteArrayDataInput)
  {
    super.onReceivePacket(packetID, dataStream)
    if (worldObj.isRemote)
    {
      if (packetID == TilePacketType.FXS.ordinal)
      {
        val `type`: Int = dataStream.readInt
        val vector: Vector3 = new Vector3(dataStream.readInt, dataStream.readInt, dataStream.readInt).translate(0.5)
        val root: Vector3 = new Vector3(this).translate(0.5)
        if (`type` == 1)
        {
          ModularForceFieldSystem.proxy.renderBeam(this.worldObj, root, vector, 0.6f, 0.6f, 1, 40)
          ModularForceFieldSystem.proxy.renderHologramMoving(this.worldObj, vector, 1, 1, 1, 50)
        }
        else if (`type` == 2)
        {
          ModularForceFieldSystem.proxy.renderBeam(this.worldObj, vector, root, 1f, 0f, 0f, 40)
          ModularForceFieldSystem.proxy.renderHologramMoving(this.worldObj, vector, 1, 0, 0, 50)
        }
      }
      else if (packetID == TilePacketType.FIELD.ordinal)
      {
        this.getCalculatedField.clear
        val nbt: NBTTagCompound = PacketHandler.readNBTTagCompound(dataStream)
        val nbtList: NBTTagList = nbt.getTagList("blockList")
        {
          var i: Int = 0
          while (i < nbtList.tagCount)
          {
            {
              val tagAt: NBTTagCompound = nbtList.tagAt(i).asInstanceOf[NBTTagCompound]
              this.getCalculatedField.add(new Vector3(tagAt))
            }
            ({
              i += 1;
              i - 1
            })
          }
        }
        this.isCalculated = true
      }
    }
  }

  def sendFieldToClient
  {
    val nbt: NBTTagCompound = new NBTTagCompound
    val nbtList: NBTTagList = new NBTTagList
    import scala.collection.JavaConversions._
    for (vector <- this.getCalculatedField)
    {
      nbtList.appendTag(vector.writeToNBT(new NBTTagCompound))
    }
    nbt.setTag("blockList", nbtList)
    PacketDispatcher.sendPacketToAllPlayers(ModularForceFieldSystem.PACKET_TILE.getPacket(this, TilePacketType.FIELD.ordinal, nbt))
  }

  protected override def calculateForceField(callBack: Nothing)
  {
    if (!this.worldObj.isRemote && !this.isCalculating)
    {
      if (this.getMode != null)
      {
        this.forceFields.clear
      }
    }
    super.calculateForceField(callBack)
    this.isCompleteConstructing = false
    this.fieldRequireTicks = false
    if (this.getModuleStacks != null)
    {
      for (module <- this.getModuleStacks)
      {
        if (module != null && (module.getItem.asInstanceOf[IModule]).requireTicks(module))
        {
          fieldRequireTicks = true
          break //todo: break is not supported
        }
      }
    }
  }

  def onThreadComplete
  {
    if (this.clientSideSimulationRequired)
    {
      this.sendFieldToClient
    }
  }

  private def clientSideSimulationRequired: Boolean =
  {
    return this.getModuleCount(ModularForceFieldSystem.itemModuleRepulsion) > 0
  }

  override def updateEntity
  {
    super.updateEntity
    if (this.isActive && this.getMode != null && this.requestFortron(this.getFortronCost, false) >= this.getFortronCost)
    {
      this.consumeCost
      if (this.ticks % 10 eq 0 || this.markFieldUpdate || this.fieldRequireTicks)
      {
        if (!this.isCalculated)
        {
          this.calculateForceField(this)
        }
        else
        {
          this.projectField
        }
      }
      if (isActive && worldObj.isRemote)
      {
        animation += getFortronCost / 100f
      }
      if (ticks % (2 * 20) eq 0 && this.getModuleCount(ModularForceFieldSystem.itemModuleSilence) <= 0)
      {
        worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, ModularForceFieldSystem.PREFIX + "field", 0.6f, (1 - this.worldObj.rand.nextFloat * 0.1f))
      }
    }
    else if (!worldObj.isRemote)
    {
      destroyField
    }
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
    return Math.max(Math.min((this.getCalculatedField.size / 1000), 10), 1)
  }

  override def onInventoryChanged
  {
    super.onInventoryChanged
    this.destroyField
  }

  /**
   * Projects a force field based on the calculations made.
   */
  def projectField
  {
    if (isCalculated && !isCalculating)
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
        var constructionCount: Int = 0
        val constructionSpeed: Int = Math.min(this.getProjectionSpeed, Settings.MAX_FORCE_FIELDS_PER_TICK)
        val fieldToBeProjected: Set[Vector3] = new HashSet[_](calculatedField)
        import scala.collection.JavaConversions._
        for (module <- this.getModules(this.getModuleSlots))
        {
          if (module.onProject(this, fieldToBeProjected))
          {
            return
          }
        }
        val it: Iterator[Vector3] = fieldToBeProjected.iterator
        while (it.hasNext)
        {
          val vector: Vector3 = it.next
          val block: Block = Block.blocksList(vector.getBlockID(this.worldObj))
          if (canReplaceBlock(vector, block))
          {
            if (block ne ModularForceFieldSystem.blockForceField && !(vector == new Vector3(this)))
            {
              if (this.worldObj.getChunkFromBlockCoords(vector.xi, vector.zi).isChunkLoaded)
              {
                constructionCount += 1
                for (module <- getModules(getModuleSlots))
                {
                  val flag: Int = module.onProject(this, vector.clone)
                  if (flag == 1)
                  {
                    continue //todo: continue is not supported
                  }
                  else if (flag == 2)
                  {
                    break //todo: label break is not supported
                  }
                }
                if (!this.worldObj.isRemote)
                {
                  this.worldObj.setBlock(vector.xi, vector.yi, vector.zi, ModularForceFieldSystem.blockForceField.blockID, 0, 2)
                }
                this.forceFields.add(vector)
                val tileEntity: TileEntity = this.worldObj.getTileEntity(vector.xi, vector.yi, vector.zi)
                if (tileEntity.isInstanceOf[TileForceField])
                {
                  (tileEntity.asInstanceOf[TileForceField]).setProjector(new Vector3(this))
                }
                this.requestFortron(1, true)
                if (constructionCount >= constructionSpeed)
                {
                  break //todo: break is not supported
                }
              }
            }
          }
        } //todo: labels is not supported
        isCompleteConstructing = constructionCount == 0
      }
    }
  }

  private def canReplaceBlock(vector: Vector3, block: Block): Boolean =
  {
    return block == null || (this.getModuleCount(ModularForceFieldSystem.itemModuleDisintegration) > 0 && block.getBlockHardness(this.worldObj, vector.xi, vector.yi, vector.zi) != -1) || block.blockMaterial.isLiquid || block eq Block.snow || block eq Block.vine || block eq Block.tallGrass || block eq Block.deadBush || block.isBlockReplaceable(this.worldObj, vector.xi, vector.yi, vector.zi)
  }

  def destroyField
  {
    if (!this.worldObj.isRemote && this.isCalculated && !this.isCalculating)
    {
      calculatedField synchronized
              {
                val copiedSet: HashSet[Vector3] = new HashSet[Vector3](calculatedField)
                val it: Iterator[Vector3] = copiedSet.iterator
                import scala.collection.JavaConversions._
                for (module <- this.getModules(this.getModuleSlots))
                {
                  if (module.onDestroy(this, this.getCalculatedField))
                  {
                    break //todo: break is not supported
                  }
                }
                while (it.hasNext)
                {
                  val vector: Vector3 = it.next
                  val block: Block = Block.blocksList(vector.getBlockID(this.worldObj))
                  if (block eq ModularForceFieldSystem.blockForceField)
                  {
                    this.worldObj.setBlock(vector.xi, vector.yi, vector.zi, 0, 0, 3)
                  }
                }
              }
    }
    this.forceFields.clear
    this.calculatedField.clear
    this.isCalculated = false
    this.isCompleteConstructing = false
    this.fieldRequireTicks = false
  }

  override def invalidate
  {
    this.destroyField
    super.invalidate
  }

  def getProjectionSpeed: Int =
  {
    return 28 + 28 * this.getModuleCount(ModularForceFieldSystem.itemModuleSpeed, this.getModuleSlots)
  }

  override def getSizeInventory: Int =
  {
    return 3 + 18
  }

  def getForceFields: Set[Vector3] =
  {
    return this.forceFields
  }

  override def isItemValidForSlot(slotID: Int, itemStack: ItemStack): Boolean =
  {
    if (slotID == 0 || slotID == 1)
    {
      return itemStack.getItem.isInstanceOf[ItemCard]
    }
    else if (slotID == moduleSlotID)
    {
      return itemStack.getItem.isInstanceOf[IProjectorMode]
    }
    else if (slotID >= 15)
    {
      return true
    }
    return itemStack.getItem.isInstanceOf[IModule]
  }

  override def getCards: Set[ItemStack] =
  {
    val cards: Set[ItemStack] = new HashSet[ItemStack]
    cards.add(super.getCard)
    cards.add(this.getStackInSlot(1))
    return cards
  }

  @SideOnly(Side.CLIENT)
  override def getRenderBoundingBox: AxisAlignedBB =
  {
    return AxisAlignedBB.getAABBPool.getAABB(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 2, this.zCoord + 1)
  }

  def getTicks: Long = ticks

  def isInField(position: Vector3) = if (getMode != null) getMode.isInField(this, position) else false

  //TODO: Finish this
  def isAccessGranted(profile: GameProfile, permission: Permission): Boolean =
  {
    if (isActive && getBiometricIdentifiers.forall(_.hasPermission(profile, permission)))
      return matrix.getModuleCount(ModularForceFieldSystem.Items.moduleInvert) == 0

    return true
  }

  def isAccessGranted(profile: GameProfile, permissions: Permission*): Boolean = permissions.forall(isAccessGranted(profile, _))

  def isAccessGranted(checkWorld: World, checkPos: Vector3, profile: GameProfile, action: PlayerInteractEvent.Action): Boolean =
  {
    var hasPermission: Boolean = true

    if (action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && checkPos.getTileEntity(checkWorld) != null)
    {
      if (getModuleCount(ModularForceFieldSystem.Items.moduleBlockAccess) > 0)
      {
        hasPermission = hasPermission(player.getGameProfile(), MFFSPermissions.blockAccess)
      }
    }

    if (hasPermission)
    {
      if (interdictionMatrix.getModuleCount(ModularForceFieldSystem.Items.moduleBlockAlter) > 0 && (player.getCurrentEquippedItem != null || action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK))
      {
        hasPermission = hasPermission(player.getGameProfile(), MFFSPermissions.blockAlter)
      }
    }

    return hasPermission
  }

}
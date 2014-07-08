package mffs.field

import java.util.{Set => JSet}

import com.mojang.authlib.GameProfile
import cpw.mods.fml.common.network.ByteBufUtils
import cpw.mods.fml.relauncher.{Side, SideOnly}
import io.netty.buffer.ByteBuf
import mffs.base.{TileFieldInteraction, TilePacketType}
import mffs.field.mode.ItemModeCustom
import mffs.item.card.ItemCard
import mffs.security.access.MFFSPermissions
import mffs.util.TCache
import mffs.{ModularForceFieldSystem, Reference, Settings}
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.util.AxisAlignedBB
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import resonant.api.mffs.IProjector
import resonant.api.mffs.modules.{IModule, IProjectorMode}
import resonant.lib.access.Permission
import resonant.lib.network.PacketTile
import universalelectricity.core.transform.region.Cuboid
import universalelectricity.core.transform.vector.Vector3

import scala.collection.JavaConversions._
import scala.collection.mutable

class TileElectromagnetProjector extends TileFieldInteraction with IProjector
{
  /** A set containing all positions of all force field blocks generated. */
  val forceFields = mutable.Set.empty[Vector3]

  /** Marks the field for an update call */
  var markFieldUpdate = true

  /** True if the field is done constructing and the projector is simply maintaining the field  */
  private var isCompleteConstructing = false

  /** True to make the field constantly tick */
  private var fieldRequireTicks = false

  bounds = new Cuboid(0, 0, 0, 1, 0.8, 1)
  capacityBase = 50
  startModuleIndex = 1
  maxSlots = 3 + 18

  override def start()
  {
    super.start()
    calculateForceField(postCalculation)
  }

  def postCalculation() = if (clientSideSimulationRequired) sendFieldToClient

  override def getLightValue(access: IBlockAccess) = if (getMode() != null) 10 else 0

  override def onReceivePacket(packetID: Int, dataStream: ByteBuf)
  {
    super.onReceivePacket(packetID, dataStream)

    if (worldObj.isRemote)
    {
      if (packetID == TilePacketType.FXS.id)
      {
        val packetType = dataStream.readInt
        val vector: Vector3 = new Vector3(dataStream.readInt, dataStream.readInt, dataStream.readInt) + 0.5
        val root: Vector3 = new Vector3(this) + 0.5

        if (packetType == 1)
        {
          ModularForceFieldSystem.proxy.renderBeam(this.worldObj, root, vector, 0.6f, 0.6f, 1, 40)
          ModularForceFieldSystem.proxy.renderHologramMoving(this.worldObj, vector, 1, 1, 1, 50)
        }
        else if (packetType == 2)
        {
          ModularForceFieldSystem.proxy.renderBeam(this.worldObj, vector, root, 1f, 0f, 0f, 40)
          ModularForceFieldSystem.proxy.renderHologramMoving(this.worldObj, vector, 1, 0, 0, 50)
        }
      }
      else if (packetID == TilePacketType.FIELD.id)
      {
        getCalculatedField.clear()
        val nbt = ByteBufUtils.readTag(dataStream)
        val nbtList = nbt.getTagList("blockList", 10)
        calculatedField ++= ((0 until nbtList.tagCount) map (i => new Vector3(nbtList.getCompoundTagAt(i))))
        isCalculated = true
      }
    }
  }

  def sendFieldToClient
  {
    val nbt = new NBTTagCompound
    val nbtList = new NBTTagList
    getCalculatedField foreach (vec => nbtList.appendTag(vec.toNBT))
    nbt.setTag("blockList", nbtList)
    ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(this, TilePacketType.FIELD.id: Integer, nbt))
  }

  /**
   * Initiate a field calculation
   */
  protected override def calculateForceField(callBack: () => Unit = null)
  {
    if (!worldObj.isRemote && !isCalculating)
    {
      if (getMode != null)
      {
        forceFields.clear
      }
    }

    super.calculateForceField(callBack)
    isCompleteConstructing = false
    fieldRequireTicks = getModuleStacks() exists (module => module.getItem.asInstanceOf[IModule].requireTicks(module))
  }

  private def clientSideSimulationRequired: Boolean =
  {
    return getModuleCount(ModularForceFieldSystem.Items.moduleRepulsion) > 0
  }

  override def update()
  {
    super.update()

    if (isActive && getMode != null && requestFortron(getFortronCost, false) >= this.getFortronCost)
    {
      consumeCost()

      if (ticks % 10 == 0 || markFieldUpdate || fieldRequireTicks)
      {
        if (!this.isCalculated)
        {
          calculateForceField(postCalculation)
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
      if (ticks % (2 * 20) == 0 && getModuleCount(ModularForceFieldSystem.Items.moduleSilence) <= 0)
      {
        worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, Reference.prefix + "field", 0.6f, (1 - this.worldObj.rand.nextFloat * 0.1f))
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

  override def markDirty()
  {
    super.markDirty()
    destroyField()
  }

  /**
   * Projects a force field based on the calculations made.
   */
  def projectField()
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
        val constructionSpeed = Math.min(getProjectionSpeed, Settings.maxForceFieldsPerTick)
        val potentialField = calculatedField.clone
        val relevantModules = getModules(getModuleSlots: _*)

        if (relevantModules.exists(_.onProject(this, potentialField)))
          return

        val evaluateField = potentialField
                .view.par
                .filter(!_.equals(position))
                .filter(v => canReplaceBlock(v, v.getBlock(world)))
                .filter(_.getBlock(world) != ModularForceFieldSystem.Blocks.forceField)
                .filter(v => world.getChunkFromBlockCoords(v.xi, v.zi).isChunkLoaded)
                .take(constructionSpeed)
                .seq

        evaluateField.forall(
          vector =>
          {
            var flag = 0

            relevantModules.exists({ module =>
              flag = module.onProject(this, vector)
              flag == 1 || flag == 2
            })

            if (flag != 1 && flag != 2)
            {
              /**
               * Default force field block placement action.
               */
              if (!world.isRemote)
                vector.setBlock(world, ModularForceFieldSystem.Blocks.forceField)

              forceFields += vector

              val tileEntity = vector.getTileEntity(world)

              if (tileEntity.isInstanceOf[TileForceField])
                (tileEntity.asInstanceOf[TileForceField]).setProjector(new Vector3(this))

              //requestFortron(1, true)
            }

            flag == 2
          })

        isCompleteConstructing = evaluateField.size == 0

        /*
        val it: Iterator[Vector3] = fieldToBeProjected.iterator
        while (it.hasNext)
        {
          val vector: Vector3 = it.next
          val block = vector.getBlock(worldObj)

          if (canReplaceBlock(vector, block))
          {
            if (block != ModularForceFieldSystem.blockForceField && !(vector == new Vector3(this)))
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
        */
      }
    }
  }

  private def canReplaceBlock(vector: Vector3, block: Block): Boolean =
  {
    return block == null ||
            (getModuleCount(ModularForceFieldSystem.Items.moduleDisintegration) > 0 && block.getBlockHardness(this.worldObj, vector.xi, vector.yi, vector.zi) != -1) ||
            (block.getMaterial.isLiquid || block == Blocks.snow || block == Blocks.vine || block == Blocks.tallgrass || block == Blocks.deadbush || block.isReplaceable(world, vector.xi, vector.yi, vector.zi))
  }

  def destroyField()
  {
    if (!this.worldObj.isRemote && this.isCalculated && !this.isCalculating)
    {
      calculatedField synchronized
              {
                getModules(getModuleSlots: _*).forall(!_.onDestroy(this, calculatedField))
                calculatedField filter (_.getBlock(world) == ModularForceFieldSystem.Blocks.forceField) foreach (_.setBlock(world, Blocks.air))
              }
    }

    forceFields.clear()
    calculatedField.clear()
    isCalculated = false
    isCompleteConstructing = false
    fieldRequireTicks = false
  }

  override def invalidate
  {
    destroyField()
    super.invalidate
  }

  def getProjectionSpeed: Int = 28 + 28 * getModuleCount(ModularForceFieldSystem.Items.moduleSpeed, getModuleSlots: _*)

  override def getForceFields: JSet[Vector3] = forceFields

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

  override def getCards: Set[ItemStack] = Set[ItemStack](super.getCard, getStackInSlot(1))

  @SideOnly(Side.CLIENT)
  override def getRenderBoundingBox: AxisAlignedBB = bounds.toAABB

  def getTicks: Long = ticks

  def isInField(position: Vector3) = if (getMode != null) getMode.isInField(this, position) else false

  override def hasPermission(profile: GameProfile, permission: Permission): Boolean =
  {
    if (super.hasPermission(profile, permission))
      return getModuleCount(ModularForceFieldSystem.Items.moduleInvert) == 0

    return true
  }

  def isAccessGranted(checkWorld: World, checkPos: Vector3, player: EntityPlayer, action: PlayerInteractEvent.Action): Boolean =
  {
    var hasPerm = true

    if (action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && checkPos.getTileEntity(checkWorld) != null)
    {
      if (getModuleCount(ModularForceFieldSystem.Items.moduleBlockAccess) > 0)
      {
        hasPerm = hasPermission(player.getGameProfile, MFFSPermissions.blockAccess)
      }
    }

    if (hasPerm)
    {
      if (getModuleCount(ModularForceFieldSystem.Items.moduleBlockAlter) > 0 && (player.getCurrentEquippedItem != null || action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK))
      {
        hasPerm = hasPermission(player.getGameProfile, MFFSPermissions.blockAlter)
      }
    }

    return hasPerm
  }

  /**
   * Rendering
   */
  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    RenderElectromagneticProjector.render(this, pos.x, pos.y, pos.z, frame)
  }
}
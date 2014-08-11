package resonantinduction.atomic.machine.reactor

import java.util.{ArrayList, List}

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{Fluid, FluidContainerRegistry, FluidStack, FluidTank, FluidTankInfo, IFluidHandler}
import resonant.api.{IReactor, IReactorComponent}
import resonant.api.event.PlasmaEvent
import resonant.engine.grid.thermal.{ThermalGrid, ThermalPhysics}
import resonant.lib.content.prefab.java.TileInventory
import resonant.lib.multiblock.reference.{IMultiBlockStructure, MultiBlockHandler}
import resonant.lib.network.Synced
import resonant.lib.network.Synced.{SyncedInput, SyncedOutput}
import resonant.lib.network.discriminator.PacketAnnotation
import resonant.lib.prefab.poison.PoisonRadiation
import resonant.lib.utility.inventory.InventoryUtility
import resonantinduction.atomic.machine.plasma.TilePlasma
import resonantinduction.atomic.{Atomic, AtomicContent}
import resonantinduction.core.Reference
import universalelectricity.core.transform.vector.{Vector3, VectorWorld}

import scala.util.control.Breaks._
import scala.collection.convert.wrapAll._

/** The primary reactor component cell used to build reactors with.
  *
  * @author Calclavia */
object TileReactorCell {
  final val RADIUS: Int = 2
  final val MELTING_POINT: Int = 2000
}

class TileReactorCell extends TileInventory(Material.iron) with IMultiBlockStructure[TileReactorCell] with IReactor with IFluidHandler {

  private final val specificHeatCapacity: Int = 1000
  private final val mass: Float = ThermalPhysics.getMass(1000, 7)
  var tank: FluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 15)
  @Synced var temperature: Float = 295
  private var previousTemperature: Float = 295
  private var shouldUpdate: Boolean = false
  private var prevInternalEnergy: Long = 0
  private var internalEnergy: Long = 0
  private var meltdownCounter: Int = 0
  private var meltdownCounterMaximum: Int = 1000
  /** Multiblock Methods. */
  private var multiBlock: MultiBlockHandler[TileReactorCell] = null

  textureName = "machine"
  isOpaqueCube = false
  normalRender = false
  customItemRender = true

  override protected def onWorldJoin {
    updatePositionStatus
  }

  override protected def onNeighborChanged(block : Block) {
    updatePositionStatus
  }

  /** Called when the block is right clicked by the player */
  override protected def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean = {
    if (!world.isRemote) {
      val tile: TileReactorCell = getMultiBlock.get()
      if (player.inventory.getCurrentItem != null) {
        if (tile.getStackInSlot(0) == null) {
          if (player.inventory.getCurrentItem.getItem.isInstanceOf[IReactorComponent]) {
            val itemStack: ItemStack = player.inventory.getCurrentItem.copy
            itemStack.stackSize = 1
            tile.setInventorySlotContents(0, itemStack)
            player.inventory.decrStackSize(player.inventory.currentItem, 1)
            return true
          }
        }
      }
      else if (player.isSneaking && tile.getStackInSlot(0) != null) {
        InventoryUtility.dropItemStack(world, new Vector3(player), tile.getStackInSlot(0), 0)
        tile.setInventorySlotContents(0, null)
        return true
      }
      else {
        player.openGui(Atomic.INSTANCE, 0, world, tile.xCoord, tile.yCoord, tile.zCoord)
      }
    }
    return true
  }

  override protected def markUpdate {
    super.markUpdate
    shouldUpdate = true
  }

  override def update {
    super.update
    if (!getMultiBlock.isPrimary) {
      if (getStackInSlot(0) != null) {
        if (getMultiBlock.get.getStackInSlot(0) == null) {
          getMultiBlock.get.setInventorySlotContents(0, getStackInSlot(0))
          setInventorySlotContents(0, null)
        }
      }
      if (tank.getFluidAmount > 0) {
        getMultiBlock.get.tank.fill(tank.drain(tank.getCapacity, true), true)
      }
    }
    if (!getWorld.isRemote) {
      if (getMultiBlock().isPrimary() && tank.getFluid != null && tank.getFluid.fluidID == AtomicContent.FLUID_PLASMA.getID) {
        val drain: FluidStack = tank.drain(FluidContainerRegistry.BUCKET_VOLUME, false)
        if (drain != null && drain.amount >= FluidContainerRegistry.BUCKET_VOLUME) {
          val spawnDir: ForgeDirection = ForgeDirection.getOrientation(worldObj.rand.nextInt(3) + 2)
          val spawnPos: Vector3 = new Vector3(this) + spawnDir + spawnDir
          spawnPos.add(0, Math.max(worldObj.rand.nextInt(getHeight) - 1, 0), 0)
          if (worldObj.isAirBlock(spawnPos.xi, spawnPos.yi, spawnPos.zi)) {
            MinecraftForge.EVENT_BUS.post(new PlasmaEvent.SpawnPlasmaEvent(worldObj, spawnPos.xi, spawnPos.yi, spawnPos.zi, TilePlasma.plasmaMaxTemperature))
            tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true)
          }
        }
      }
      else {
        prevInternalEnergy = internalEnergy
        val fuelRod: ItemStack = getMultiBlock.get.getStackInSlot(0)
        if (fuelRod != null) {
          if (fuelRod.getItem.isInstanceOf[IReactorComponent]) {
            (fuelRod.getItem.asInstanceOf[IReactorComponent]).onReact(fuelRod, this)
            if (!worldObj.isRemote) {
              if (fuelRod.getItemDamage >= fuelRod.getMaxDamage) {
                getMultiBlock.get.setInventorySlotContents(0, null)
              }
            }
            if (ticks % 20 == 0) {
              if (worldObj.rand.nextFloat > 0.65) {
                val entities: List[EntityLiving] = worldObj.getEntitiesWithinAABB(classOf[EntityLiving], AxisAlignedBB.getBoundingBox(xCoord - TileReactorCell.RADIUS * 2, yCoord - TileReactorCell.RADIUS * 2, zCoord - TileReactorCell.RADIUS * 2, xCoord + TileReactorCell.RADIUS * 2, yCoord + TileReactorCell.RADIUS * 2, zCoord + TileReactorCell.RADIUS * 2)).asInstanceOf[List[EntityLiving]]
                for (entity <- entities) {
                  PoisonRadiation.INSTANCE.poisonEntity(new Vector3(this), entity)
                }
              }
            }
          }
        }
        temperature = ThermalGrid.getTemperature(new VectorWorld(this))
        if (internalEnergy - prevInternalEnergy > 0) {
          var deltaT: Float = ThermalPhysics.getTemperatureForEnergy(mass, specificHeatCapacity, ((internalEnergy - prevInternalEnergy) * 0.15).asInstanceOf[Long])
          var rods: Int = 0

            for(i <- 0 to 5) {
              {
                val checkAdjacent: Vector3 = new Vector3(this).add(ForgeDirection.getOrientation(i))
                if (checkAdjacent.getBlock(worldObj) == AtomicContent.blockControlRod) {
                  deltaT /= 1.1f
                  rods += 1
                }
              }
          }
          ThermalGrid.addTemperature(new VectorWorld(this), deltaT)
          if (worldObj.rand.nextInt(80) == 0 && this.getTemperature >= 373) {
            worldObj.playSoundEffect(this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, "Fluid.lava", 0.5F, 2.1F + (worldObj.rand.nextFloat - worldObj.rand.nextFloat) * 0.85F)
          }
          if (worldObj.rand.nextInt(40) == 0 && this.getTemperature >= 373) {
            worldObj.playSoundEffect(this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, "Fluid.lavapop", 0.5F, 2.6F + (worldObj.rand.nextFloat - worldObj.rand.nextFloat) * 0.8F)
          }
          if (worldObj.getWorldTime % (Atomic.SECOND_IN_TICKS * 5.0F) == 0 && this.getTemperature >= 373) {
            val percentage: Float = Math.min(this.getTemperature / TileReactorCell.MELTING_POINT, 1.0F)
            worldObj.playSoundEffect(this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, Reference.prefix + "reactorcell", percentage, 1.0F)
          }
          if (previousTemperature != temperature && !shouldUpdate) {
            shouldUpdate = true
            previousTemperature = temperature
          }
          if (previousTemperature >= TileReactorCell.MELTING_POINT && meltdownCounter < meltdownCounterMaximum) {
            shouldUpdate = true
            meltdownCounter += 1
          }
          else if (previousTemperature >= TileReactorCell.MELTING_POINT && meltdownCounter >= meltdownCounterMaximum) {
            meltdownCounter = 0
            meltDown
            return
          }
          if (previousTemperature < TileReactorCell.MELTING_POINT && meltdownCounter < meltdownCounterMaximum && meltdownCounter > 0) {
            meltdownCounter -= 1
          }
        }
        internalEnergy = 0
        if (isOverToxic) {
          val leakPos: VectorWorld = new VectorWorld(this).add(worldObj.rand.nextInt(20) - 10, worldObj.rand.nextInt(20) - 10, worldObj.rand.nextInt(20) - 10)
          val block : Block = leakPos.getBlock
          if (block == Blocks.grass) {
            leakPos.setBlock(world, AtomicContent.blockRadioactive)
            tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true)
          }
          else if (block == null) {
            if (tank.getFluid != null) {
              leakPos.setBlock(world, tank.getFluid.getFluid.getBlock)
              tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true)
            }
          }
        }
      }
      if (ticks % 60 == 0 || shouldUpdate) {
        shouldUpdate = false
        notifyChange
        sendPacket(getDescPacket)
      }
    }
    else {
      if (worldObj.rand.nextInt(5) == 0 && this.getTemperature >= 373) {
        worldObj.spawnParticle("cloud", this.xCoord + worldObj.rand.nextInt(2), this.yCoord + 1.0F, this.zCoord + worldObj.rand.nextInt(2), 0, 0.1D, 0)
        worldObj.spawnParticle("bubble", this.xCoord + worldObj.rand.nextInt(5), this.yCoord, this.zCoord + worldObj.rand.nextInt(5), 0, 0, 0)
      }
    }
  }

  def isOverToxic: Boolean = {
    return tank.getFluid != null && tank.getFluid.fluidID == AtomicContent.FLUID_TOXIC_WASTE.getID && tank.getFluid.amount >= tank.getCapacity
  }

  /** Multiblock Methods */
  def updatePositionStatus {
    val mainTile: TileReactorCell = getLowest
    mainTile.getMultiBlock.deconstruct
    mainTile.getMultiBlock.construct
    val top: Boolean = new Vector3(this).add(new Vector3(0, 1, 0)).getTileEntity(worldObj).isInstanceOf[TileReactorCell]
    val bottom: Boolean = new Vector3(this).add(new Vector3(0, -1, 0)).getTileEntity(worldObj).isInstanceOf[TileReactorCell]
    if (top && bottom) {
      worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 3)
    }
    else if (top) {
      worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 3)
    }
    else {
      worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 2, 3)
    }
  }

  def onMultiBlockChanged {
  }

  def getMultiBlockVectors: Array[Vector3] = {
    val vectors: List[Vector3] = new ArrayList[Vector3]
    val checkPosition: Vector3 = new Vector3(this)
    while (true) {
      val t: TileEntity = checkPosition.getTileEntity(this.worldObj)
      if (t.isInstanceOf[TileReactorCell]) {
        vectors.add(checkPosition.clone.subtract(getPosition))
      }
      else {
        break //todo: break is not supported
      }
      checkPosition.y += 1
    }
    return vectors.toArray(new Array[Vector3](0))
  }

  def getLowest: TileReactorCell = {
    var lowest: TileReactorCell = this
    val checkPosition: Vector3 = new Vector3(this)
    while (true) {
      val t: TileEntity = checkPosition.getTileEntity(this.worldObj)
      if (t.isInstanceOf[TileReactorCell]) {
        lowest = t.asInstanceOf[TileReactorCell]
      }
      else {
        break //todo: break is not supported
      }
      checkPosition.y -= 1
    }
    return lowest
  }

  def getWorld: World = {
    return worldObj
  }

  def getPosition: Vector3 = {
    return new Vector3(this)
  }

  override def getMultiBlock: MultiBlockHandler[TileReactorCell] = {
    if (multiBlock == null) {
      multiBlock = new MultiBlockHandler[TileReactorCell](this)
    }
    return multiBlock
  }

  def getHeight: Int = {
    var height: Int = 0
    val checkPosition: Vector3 = new Vector3(this)
    var tile: TileEntity = this
    while (tile.isInstanceOf[TileReactorCell]) {
      checkPosition.y += 1
      height += 1
      tile = checkPosition.getTileEntity(worldObj)
    }
    return height
  }

  override def getDescriptionPacket: Any = {
    return new PacketAnnotation(this)
  }

  private def meltDown {
    if (!worldObj.isRemote) {
      this.worldObj.setBlock(this.xCoord, this.yCoord, this.zCoord, Blocks.lava)
      //val reactorExplosion: ReactorExplosion = new ReactorExplosion(worldObj, null, xCoord, yCoord, zCoord, 9f)
      //reactorExplosion.doExplosionA
      //reactorExplosion.doExplosionB(true)
    }
  }

  /** Reads a tile entity from NBT. */
  @SyncedInput override def readFromNBT(nbt: NBTTagCompound) {
    super.readFromNBT(nbt)
    temperature = nbt.getFloat("temperature")
    tank.readFromNBT(nbt)
    getMultiBlock.load(nbt)
  }

  /** Writes a tile entity to NBT. */
  @SyncedOutput override def writeToNBT(nbt: NBTTagCompound) {
    super.writeToNBT(nbt)
    nbt.setFloat("temperature", temperature)
    tank.writeToNBT(nbt)
    getMultiBlock.save(nbt)
  }

  override def getInventoryStackLimit: Int = {
    return 1
  }

  /** Returns true if automation can insert the given item in the given slot from the given side.
    * Args: Slot, item, side */
  override def canInsertItem(slot: Int, items: ItemStack, side: Int): Boolean = {
    return this.isItemValidForSlot(slot, items)
  }

  override def isUseableByPlayer(par1EntityPlayer: EntityPlayer): Boolean = {
    return if (worldObj.getTileEntity(xCoord, yCoord, zCoord) ne this) false else par1EntityPlayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D
  }

  def getInvName: String = {
    return getBlockType.getLocalizedName
  }

  override def isItemValidForSlot(slotID: Int, itemStack: ItemStack): Boolean = {
    if (getMultiBlock.isPrimary && getMultiBlock.get.getStackInSlot(0) == null) {
      return itemStack.getItem.isInstanceOf[IReactorComponent]
    }
    return false
  }

  /** Fluid Functions. */
  override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int = {
    return getMultiBlock.get.tank.fill(resource, doFill)
  }

  override def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack = {
    return tank.drain(maxDrain, doDrain)
  }

  override def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack = {
    if (resource == null || !resource.isFluidEqual(tank.getFluid)) {
      return null
    }
    return tank.drain(resource.amount, doDrain)
  }

  override def canFill(from: ForgeDirection, fluid: Fluid): Boolean = {
    return fluid == AtomicContent.FLUID_PLASMA
  }

  override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean = {
    return fluid == AtomicContent.FLUID_TOXIC_WASTE
  }

  override def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] = {
    return Array[FluidTankInfo](tank.getInfo)
  }

  @SideOnly(Side.CLIENT)  override def getRenderBoundingBox: AxisAlignedBB = {
    if (getMultiBlock.isPrimary && getMultiBlock.isConstructed) {
      return AxisAlignedBB.getBoundingBox(x - 5, y - 5, z - 5, x + 5, y + 5, z + 5);
    }
    return super.getRenderBoundingBox
  }

  def heat(energy: Long) {
    internalEnergy = Math.max(internalEnergy + energy, 0)
  }

  def getTemperature: Float = {
    return temperature
  }
}
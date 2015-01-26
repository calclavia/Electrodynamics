package edx.quantum.reactor

import java.util
import java.util.List

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.{Electrodynamics, Reference}
import edx.quantum.QuantumContent
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{AxisAlignedBB, ResourceLocation}
import net.minecraft.world.World
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11
import resonantengine.api.edx.machine.{IReactor, IReactorComponent}
import resonantengine.lib.grid.thermal.{GridThermal, ThermalPhysics}
import resonantengine.prefab.network.{TPacketReceiver, TPacketSender}
import resonantengine.lib.prefab.poison.PoisonRadiation
import resonantengine.lib.prefab.tile.mixed.TileInventory
import resonantengine.lib.prefab.tile.multiblock.reference.{IMultiBlockStructure, MultiBlockHandler}
import resonantengine.lib.render.RenderUtility
import resonantengine.lib.render.model.ModelCube
import resonantengine.lib.transform.vector.Vector3
import resonantengine.lib.utility.inventory.InventoryUtility

import scala.collection.convert.wrapAll._

/** The primary reactor component cell used to build reactors with.
  *
  * @author Calclavia */
object TileReactorCell
{
  final val radius = 2
  final val meltingPoint = 3000
  final val specificHeatCapacity = 1000
  final val mass = ThermalPhysics.getMass(1000, 7)

  final val modelTop = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "reactorCellTop.tcn"))
  final val modelMiddle = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "reactorCellMiddle.tcn"))
  final val modelBottom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "reactorCellBottom.tcn"))
  final val textureTop = new ResourceLocation(Reference.domain, Reference.modelPath + "reactorCellTop.png")
  final val textureMiddle = new ResourceLocation(Reference.domain, Reference.modelPath + "reactorCellMiddle.png")
  final val textureBottom = new ResourceLocation(Reference.domain, Reference.modelPath + "reactorCellBottom.png")
  final val textureFuel = new ResourceLocation(Reference.domain, Reference.modelPath + "fissileMaterial.png")
}

class TileReactorCell extends TileInventory(Material.iron) with IMultiBlockStructure[TileReactorCell] with IReactor with TPacketSender with TPacketReceiver
{
  /** Multiblock Methods. */
  private val multiBlock = new MultiBlockHandler[TileReactorCell](this)
  private var internalEnergy = 0d

  textureName = "machine"
  isOpaqueCube = false
  normalRender = false

  override def getSizeInventory = 1

  override def onWorldJoin()
  {
    updatePositionStatus()
  }

  /** Multiblock Methods */
  def updatePositionStatus()
  {
    val mainTile = getLowest
    mainTile.getMultiBlock.deconstruct()
    mainTile.getMultiBlock.construct()

    val top = (toVector3 + new Vector3(0, 1, 0)).getTileEntity(worldObj).isInstanceOf[TileReactorCell]
    val bottom = (toVector3 + new Vector3(0, -1, 0)).getTileEntity(worldObj).isInstanceOf[TileReactorCell]

    if (top && bottom)
    {
      setMeta(1)
    }
    else if (top)
    {
      setMeta(0)
    }
    else
    {
      setMeta(2)
    }
  }

  /**
   * @return Gets the lowest reactor cell in this "tower"
   */
  def getLowest: TileReactorCell =
  {
    var lowest: TileReactorCell = this
    val checkPosition: Vector3 = toVector3
    while (true)
    {
      val t: TileEntity = checkPosition.getTileEntity(this.worldObj)
      if (t.isInstanceOf[TileReactorCell])
      {
        lowest = t.asInstanceOf[TileReactorCell]
      }
      else
      {
        return lowest
      }
      checkPosition.y -= 1
    }
    return lowest
  }

  override def getMultiBlock: MultiBlockHandler[TileReactorCell] = multiBlock

  override def onNeighborChanged(block: Block)
  {
    updatePositionStatus()
  }

  override def update()
  {
    super.update()

    /**
     * Move the fuel rod down into the main reactor.
     */
    if (!getMultiBlock.isPrimary)
    {
      if (getStackInSlot(0) != null)
      {
        if (getMultiBlock.get.getStackInSlot(0) == null)
        {
          getMultiBlock.get.setInventorySlotContents(0, getStackInSlot(0))
          setInventorySlotContents(0, null)
        }
      }
    }

    if (!getWorld.isRemote)
    {
      /*
      if (getMultiBlock().isPrimary() && tank.getFluid != null && tank.getFluid.fluidID == QuantumContent.FLUID_PLASMA.getID)
      {
        val drain: FluidStack = tank.drain(FluidContainerRegistry.BUCKET_VOLUME, false)
        if (drain != null && drain.amount >= FluidContainerRegistry.BUCKET_VOLUME)
        {
          val spawnDir: ForgeDirection = ForgeDirection.getOrientation(worldObj.rand.nextInt(3) + 2)
          val spawnPos: Vector3 = toVector3 + spawnDir + spawnDir
          spawnPos.add(0, Math.max(worldObj.rand.nextInt(getHeight) - 1, 0), 0)
          if (worldObj.isAirBlock(spawnPos.xi, spawnPos.yi, spawnPos.zi))
          {
            MinecraftForge.EVENT_BUS.post(new PlasmaEvent.SpawnPlasmaEvent(worldObj, spawnPos.xi, spawnPos.yi, spawnPos.zi, TilePlasma.plasmaMaxTemperature))
            tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true)
          }
        }
      }
      else
      {*/

      /**
       * Fission reaction
       */
      val fuelRod = getMultiBlock.get.getStackInSlot(0)

      if (fuelRod != null)
      {
        if (fuelRod.getItem.isInstanceOf[IReactorComponent])
        {
          fuelRod.getItem.asInstanceOf[IReactorComponent].onReact(fuelRod, this)

          if (!worldObj.isRemote)
          {
            if (fuelRod.getItemDamage >= fuelRod.getMaxDamage)
            {
              getMultiBlock.get.setInventorySlotContents(0, null)
            }
          }

          /**
           * Radiation
           */
          //TODO: Raycast radiation code
          if (ticks % 20 == 0)
          {
            if (worldObj.rand.nextFloat > 0.65)
            {
              val entities = worldObj.getEntitiesWithinAABB(classOf[EntityLiving], AxisAlignedBB.getBoundingBox(xCoord - TileReactorCell.radius * 2, yCoord - TileReactorCell.radius * 2, zCoord - TileReactorCell.radius * 2, xCoord + TileReactorCell.radius * 2, yCoord + TileReactorCell.radius * 2, zCoord + TileReactorCell.radius * 2)).asInstanceOf[List[EntityLiving]]
              for (entity <- entities)
              {
                PoisonRadiation.INSTANCE.poisonEntity(toVector3, entity)
              }
            }
          }
        }
      }

      /**
       * Heats up the surroundings. Control rods absorbs neutrons, reducing the heat produced.
       */
      val controlRodCount = ForgeDirection.VALID_DIRECTIONS.map(toVectorWorld + _).count(_.getBlock == QuantumContent.blockControlRod)
      GridThermal.addHeat(toVectorWorld, internalEnergy / ((controlRodCount + 1) * 0.3))
      val temperature = GridThermal.getTemperature(toVectorWorld)

      internalEnergy = 0

      /**
       * Play sound effects
       */
      if (temperature >= 373)
      {
        if (world.rand.nextInt(80) == 0)
        {
          world.playSoundEffect(xCoord + 0.5F, yCoord + 0.5F, zCoord + 0.5F, "Fluid.lava", 0.5F, 2.1F + (worldObj.rand.nextFloat - worldObj.rand.nextFloat) * 0.85F)
        }
        if (world.rand.nextInt(40) == 0)
        {
          world.playSoundEffect(xCoord + 0.5F, yCoord + 0.5F, zCoord + 0.5F, "Fluid.lavapop", 0.5F, 2.6F + (worldObj.rand.nextFloat - worldObj.rand.nextFloat) * 0.8F)
        }

        if (ticks % (20 * 5) == 0)
        {
          world.playSoundEffect(this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F, Reference.prefix + "reactorcell", temperature / TileReactorCell.meltingPoint, 1.0F)
        }
      }

      if (temperature > TileReactorCell.meltingPoint)
      {
        //        meltDown()
      }
    }
    else
    {
      val temperature = GridThermal.getTemperature(toVectorWorld)

      if (world.rand.nextInt(5) == 0 && temperature >= 373)
      {
        world.spawnParticle("cloud", this.xCoord + worldObj.rand.nextInt(2), this.yCoord + 1.0F, this.zCoord + worldObj.rand.nextInt(2), 0, 0.1D, 0)
        world.spawnParticle("bubble", this.xCoord + worldObj.rand.nextInt(5), this.yCoord, this.zCoord + worldObj.rand.nextInt(5), 0, 0, 0)
      }
    }
  }

  override def getWorld: World =
  {
    return worldObj
  }

  def onMultiBlockChanged()
  {
  }

  override def getMultiBlockVectors: java.lang.Iterable[Vector3] =
  {
    val vectors: List[Vector3] = new util.ArrayList[Vector3]
    val checkPosition: Vector3 = toVector3
    while (true)
    {
      val t: TileEntity = checkPosition.getTileEntity(this.worldObj)
      if (t.isInstanceOf[TileReactorCell])
      {
        vectors.add(checkPosition - getPosition)
      }
      else
      {
        return vectors
      }
      checkPosition.y += 1
    }
    return vectors
  }

  def getPosition: Vector3 =
  {
    return toVector3
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    getMultiBlock.load(nbt)
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    getMultiBlock.save(nbt)
  }

  override def getInventoryStackLimit: Int = 1

  /** Returns true if automation can insert the given item in the given slot from the given side.
    * Args: Slot, item, side */
  override def canInsertItem(slot: Int, items: ItemStack, side: Int): Boolean =
  {
    return this.isItemValidForSlot(slot, items)
  }

  override def isItemValidForSlot(slotID: Int, itemStack: ItemStack): Boolean =
  {
    if (getMultiBlock.isPrimary && getMultiBlock.get.getStackInSlot(0) == null)
    {
      return itemStack.getItem.isInstanceOf[IReactorComponent]
    }
    return false
  }

  override def isUseableByPlayer(par1EntityPlayer: EntityPlayer): Boolean =
  {
    return if (worldObj.getTileEntity(xCoord, yCoord, zCoord) ne this) false else par1EntityPlayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D
  }

  def getInvName: String =
  {
    return getBlockType.getLocalizedName
  }

  @SideOnly(Side.CLIENT)
  override def getRenderBoundingBox: AxisAlignedBB =
  {
    if (getMultiBlock.isPrimary && getMultiBlock.isConstructed)
    {
      return AxisAlignedBB.getBoundingBox(x - 5, y - 5, z - 5, x + 5, y + 5, z + 5);
    }
    return super.getRenderBoundingBox
  }

  override def heat(energy: Double)
  {
    internalEnergy = Math.max(internalEnergy + energy, 0)
  }

  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    GL11.glPushMatrix()
    GL11.glTranslated(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
    val meta = if (frame != 0) metadata else 2

    val hasBelow = if (frame != 0) world.getTileEntity(xi, yi - 1, zi).isInstanceOf[TileReactorCell] else false

    if (meta == 0)
    {
      RenderUtility.bind(TileReactorCell.textureBottom)
      TileReactorCell.modelBottom.renderAll()
    }
    else if (meta == 1)
    {
      RenderUtility.bind(TileReactorCell.textureMiddle)
      GL11.glTranslatef(0, 0.075f, 0)
      GL11.glScalef(1f, 1.15f, 1f)
      TileReactorCell.modelMiddle.renderAll()
    }
    else
    {
      RenderUtility.bind(TileReactorCell.textureTop)

      if (hasBelow)
      {
        GL11.glScalef(1f, 1.32f, 1f)
      }
      else
      {
        GL11.glTranslatef(0, 0.1f, 0)
        GL11.glScalef(1f, 1.2f, 1f)
      }

      if (hasBelow)
      {
        TileReactorCell.modelTop.renderAllExcept("BottomPad", "BaseDepth", "BaseWidth", "Base")
      }
      else
      {
        TileReactorCell.modelTop.renderAll()
      }
    }
    GL11.glPopMatrix()

    if (getStackInSlot(0) != null)
    {
      val height = getHeight * ((getStackInSlot(0).getMaxDamage - getStackInSlot(0).getItemDamage).toFloat / getStackInSlot(0).getMaxDamage.toFloat)
      GL11.glPushMatrix()
      GL11.glTranslated(pos.x + 0.5, pos.y + 0.5 * height, pos.z + 0.5)
      GL11.glScalef(0.4f, 0.9f * height, 0.4f)
      RenderUtility.bind(TileReactorCell.textureFuel)
      RenderUtility.disableLighting()
      ModelCube.INSTNACE.render()
      RenderUtility.enableLighting()
      GL11.glPopMatrix()
    }
  }

  def getHeight: Int =
  {
    var height: Int = 0
    val checkPosition: Vector3 = toVector3
    var tile: TileEntity = this
    while (tile.isInstanceOf[TileReactorCell])
    {
      checkPosition.y += 1
      height += 1
      tile = checkPosition.getTileEntity(worldObj)
    }
    return height
  }

  /** Called when the block is right clicked by the player */
  override protected def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (!world.isRemote)
    {
      val tile: TileReactorCell = getMultiBlock.get()
      if (player.inventory.getCurrentItem != null)
      {
        if (tile.getStackInSlot(0) == null)
        {
          if (player.inventory.getCurrentItem.getItem.isInstanceOf[IReactorComponent])
          {
            val itemStack: ItemStack = player.inventory.getCurrentItem.copy
            itemStack.stackSize = 1
            tile.setInventorySlotContents(0, itemStack)
            player.inventory.decrStackSize(player.inventory.currentItem, 1)
            return true
          }
        }
      }
      else if (player.isSneaking && tile.getStackInSlot(0) != null)
      {
        InventoryUtility.dropItemStack(world, new Vector3(player), tile.getStackInSlot(0), 0)
        tile.setInventorySlotContents(0, null)
        return true
      }
      else
      {
        player.openGui(Electrodynamics, 0, world, tile.xCoord, tile.yCoord, tile.zCoord)
      }
    }

    return false
  }

  private def meltDown()
  {
    if (!world.isRemote)
    {
      world.setBlock(this.xCoord, this.yCoord, this.zCoord, Blocks.lava)
      world.createExplosion(null, x + 0.5, y + 0.5, z + 0.5, 3, false)
    }
  }
}
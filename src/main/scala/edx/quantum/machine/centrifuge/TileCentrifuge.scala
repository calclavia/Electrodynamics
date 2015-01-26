package edx.quantum.machine.centrifuge

import edx.core.Settings
import edx.quantum.QuantumContent
import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._
import resonantengine.api.network.IPacketReceiver
import resonantengine.lib.content.prefab.{TIO, TInventory}
import resonantengine.lib.grid.core.TBlockNodeProvider
import resonantengine.lib.grid.energy.EnergyStorage
import resonantengine.lib.grid.energy.electric.NodeElectricComponent
import resonantengine.lib.mod.compat.energy.Compatibility
import resonantengine.lib.network.discriminator.{PacketTile, PacketType}
import resonantengine.lib.prefab.tile.spatial.ResonantTile
import resonantengine.lib.prefab.tile.traits.{TEnergyProvider, TRotatable}
import resonantengine.lib.transform.vector.Vector3

/**
 * Centrifuge TileEntity
 */
object TileCentrifuge
{
  final val SHI_JIAN: Int = 20 * 60
  final val DIAN: Long = 500000
}

class TileCentrifuge extends ResonantTile(Material.iron) with TInventory with TBlockNodeProvider with IPacketReceiver with IFluidHandler with IInventory with TEnergyProvider with TRotatable with TIO
{
  val gasTank: FluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 5)
  private val electricNode = new NodeElectricComponent(this)
  var timer: Int = 0
  var rotation: Float = 0

  energy = new EnergyStorage
  isOpaqueCube = false
  normalRender = false
  energy.max = TileCentrifuge.DIAN * 2

  override def getSizeInventory: Int = 4

  override def update
  {
    super.update
    if (timer > 0)
    {
      rotation += 0.45f
    }
    if (!this.worldObj.isRemote)
    {
      if (this.ticks % 20 == 0)
      {
        for (i <- 0 to 6)
        {
          val direction: ForgeDirection = ForgeDirection.getOrientation(i)
          val tileEntity: TileEntity = toVector3.add(direction).getTileEntity(world)
          if (tileEntity.isInstanceOf[IFluidHandler] && tileEntity.getClass != this.getClass)
          {
            val fluidHandler: IFluidHandler = (tileEntity.asInstanceOf[IFluidHandler])
            if (fluidHandler != null)
            {
              val requestFluid: FluidStack = QuantumContent.fluidStackUraniumHexaflouride
              requestFluid.amount = this.gasTank.getCapacity - QuantumContent.getFluidAmount(this.gasTank.getFluid)
              val receiveFluid: FluidStack = fluidHandler.drain(direction.getOpposite, requestFluid, true)
              if (receiveFluid != null)
              {
                if (receiveFluid.amount > 0)
                {
                  if (this.gasTank.fill(receiveFluid, false) > 0)
                  {
                    this.gasTank.fill(receiveFluid, true)
                  }
                }
              }
            }
          }
        }
      }
      if (this.nengYong)
      {
        //discharge(getStackInSlot(0))
        if (energy >= TileCentrifuge.DIAN)
        {
          if (this.timer == 0)
          {
            this.timer = TileCentrifuge.SHI_JIAN
          }
          if (this.timer > 0)
          {
            this.timer -= 1
            if (this.timer < 1)
            {
              this.yong
              this.timer = 0
            }
          }
          else
          {
            this.timer = 0
          }
          energy -= TileCentrifuge.DIAN
        }
      }
      else
      {
        this.timer = 0
      }
      if (this.ticks % 10 == 0)
      {
      }
    }
  }

  /**
   * Turn one item from the furnace source stack into the appropriate smelted item in the furnace result stack
   */
  def yong
  {
    if (this.nengYong)
    {
      this.gasTank.drain(Settings.uraniumHexaflourideRatio, true)
      if (this.worldObj.rand.nextFloat > 0.6)
      {
        this.incrStackSize(2, new ItemStack(QuantumContent.itemUranium))
      }
      else
      {
        this.incrStackSize(3, new ItemStack(QuantumContent.itemUranium, 1, 1))
      }
    }
  }

  /**
   * @return If the machine can be used.
   */
  def nengYong: Boolean =
  {
    if (this.gasTank.getFluid != null)
    {
      if (this.gasTank.getFluid.amount >= Settings.uraniumHexaflourideRatio)
      {
        return isItemValidForSlot(2, new ItemStack(QuantumContent.itemUranium)) && isItemValidForSlot(3, new ItemStack(QuantumContent.itemUranium, 1, 1))
      }
    }
    return false
  }

  override def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    openGui(player, QuantumContent)
    return true
  }

  def read(data: ByteBuf, player: EntityPlayer, `type`: PacketType)
  {
    this.timer = data.readInt
    this.gasTank.setFluid(new FluidStack(QuantumContent.fluidStackUraniumHexaflouride.fluidID, data.readInt))

  }

  override def getDescPacket: PacketTile =
  {
    return new PacketTile(xi, yi, zi, Array[Any](this.timer, QuantumContent.getFluidAmount(this.gasTank.getFluid)))
  }

  /**
   * Reads a tile entity from NBT.
   */
  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    this.timer = nbt.getInteger("smeltingTicks")
    val compound: NBTTagCompound = nbt.getCompoundTag("gas")
    this.gasTank.setFluid(FluidStack.loadFluidStackFromNBT(compound))
  }

  /**
   * Writes a tile entity to NBT.
   */
  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setInteger("smeltingTicks", this.timer)
    if (this.gasTank.getFluid != null)
    {
      val compound: NBTTagCompound = new NBTTagCompound
      this.gasTank.getFluid.writeToNBT(compound)
      nbt.setTag("gas", compound)
    }
  }

  /**
   * Tank Methods
   */
  def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
  {
    if (QuantumContent.fluidStackUraniumHexaflouride.isFluidEqual(resource))
    {
      return this.gasTank.fill(resource, doFill)
    }
    return 0
  }

  def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack =
  {
    return null
  }

  def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack =
  {
    return null
  }

  def canFill(from: ForgeDirection, fluid: Fluid): Boolean =
  {
    return QuantumContent.fluidStackUraniumHexaflouride.fluidID == fluid.getID
  }

  def canDrain(from: ForgeDirection, fluid: Fluid): Boolean =
  {
    return false
  }

  def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] =
  {
    return Array[FluidTankInfo](this.gasTank.getInfo)
  }

  /**
   * Inventory
   */
  override def getAccessibleSlotsFromSide(side: Int): Array[Int] =
  {
    return if (side == 1) Array[Int](0, 1) else Array[Int](2, 3)
  }

  override def canInsertItem(slotID: Int, itemStack: ItemStack, side: Int): Boolean =
  {
    return slotID == 1 && this.isItemValidForSlot(slotID, itemStack)
  }

  override def isItemValidForSlot(i: Int, itemStack: ItemStack): Boolean =
  {
    i match
    {
      case 0 =>
        return Compatibility.isHandler(itemStack.getItem, null)
      case 1 =>
        return true
      case 2 =>
        return itemStack.getItem eq QuantumContent.itemUranium
      case 3 =>
        return itemStack.getItem eq QuantumContent.itemUranium
    }
    return false
  }

  override def canExtractItem(slotID: Int, itemstack: ItemStack, j: Int): Boolean =
  {
    return slotID == 2 || slotID == 3
  }
}
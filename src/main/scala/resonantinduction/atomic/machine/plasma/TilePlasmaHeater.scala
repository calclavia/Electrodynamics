package resonantinduction.atomic.machine.plasma

import java.util.HashMap

import cpw.mods.fml.common.network.ByteBufUtils
import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._
import resonant.api.tile.ITagRender
import resonant.engine.ResonantEngine
import resonant.lib.content.prefab.TEnergyStorage
import resonant.lib.grid.energy.EnergyStorage
import resonant.lib.mod.config.Config
import resonant.lib.network.discriminator.{PacketTile, PacketType}
import resonant.lib.network.handle.IPacketReceiver
import resonant.lib.prefab.tile.TileElectric
import resonant.lib.transform.vector.Vector3
import resonant.lib.utility.science.UnitDisplay
import resonant.lib.utility.{FluidUtility, LanguageUtility}
import resonantinduction.atomic.AtomicContent

object TilePlasmaHeater
{
  var joules: Long = 10000000000L
  @Config var plasmaHeatAmount: Int = 100
}

class TilePlasmaHeater extends TileElectric(Material.iron) with IPacketReceiver with ITagRender with IFluidHandler with TEnergyStorage
{
  final val tankInputDeuterium: FluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 10)
  final val tankInputTritium: FluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 10)
  final val tankOutput: FluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 10)
  var rotation: Float = 0

  //Constructor

  //TODO: Dummy
  energy = new EnergyStorage(0)
  energy.setCapacity(TilePlasmaHeater.joules)
  energy.setMaxTransfer(TilePlasmaHeater.joules / 20)
  normalRender(false)
  isOpaqueCube(false)

  override def update()
  {
    super.update()
    rotation = (rotation + energy.getEnergy / 10000f).asInstanceOf[Float]
    if (!worldObj.isRemote)
    {
      if (energy.checkExtract)
      {
        if (tankInputDeuterium.getFluidAmount >= TilePlasmaHeater.plasmaHeatAmount && tankInputTritium.getFluidAmount >= TilePlasmaHeater.plasmaHeatAmount && tankOutput.getFluidAmount < tankOutput.getCapacity)
        {
          tankInputDeuterium.drain(TilePlasmaHeater.plasmaHeatAmount, true)
          tankInputTritium.drain(TilePlasmaHeater.plasmaHeatAmount, true)
          tankOutput.fill(new FluidStack(AtomicContent.FLUID_PLASMA, tankOutput.getCapacity), true)
          energy.extractEnergy
        }
      }
    }
    if (ticks % 80 == 0)
    {
      world.markBlockForUpdate(xi, yi, zi)
    }
  }

  override def getDescriptionPacket: Packet =
  {
    val nbt: NBTTagCompound = new NBTTagCompound
    writeToNBT(nbt)
    return ResonantEngine.packetHandler.toMCPacket(new PacketTile(this, nbt))
  }

  /**
   * Writes a tile entity to NBT.
   */
  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    if (tankInputDeuterium.getFluid != null)
    {
      val compound: NBTTagCompound = new NBTTagCompound
      tankInputDeuterium.getFluid.writeToNBT(compound)
      nbt.setTag("tankInputDeuterium", compound)
    }
    if (tankInputTritium.getFluid != null)
    {
      val compound: NBTTagCompound = new NBTTagCompound
      tankInputTritium.getFluid.writeToNBT(compound)
      nbt.setTag("tankInputTritium", compound)
    }
    if (tankOutput.getFluid != null)
    {
      val compound: NBTTagCompound = new NBTTagCompound
      tankOutput.getFluid.writeToNBT(compound)
      nbt.setTag("tankOutput", compound)
    }
  }

  def read(data: ByteBuf, player: EntityPlayer, `type`: PacketType)
  {
    try
    {
      readFromNBT(ByteBufUtils.readTag(data))
    }
    catch
      {
        case e: Exception =>
        {
          e.printStackTrace
        }
      }
  }

  /**
   * Reads a tile entity from NBT.
   */
  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    val deuterium: NBTTagCompound = nbt.getCompoundTag("tankInputDeuterium")
    tankInputDeuterium.setFluid(FluidStack.loadFluidStackFromNBT(deuterium))
    val tritium: NBTTagCompound = nbt.getCompoundTag("tankInputTritium")
    tankInputTritium.setFluid(FluidStack.loadFluidStackFromNBT(tritium))
    val output: NBTTagCompound = nbt.getCompoundTag("tankOutput")
    tankOutput.setFluid(FluidStack.loadFluidStackFromNBT(output))
  }

  def addInformation(map: HashMap[String, Integer], player: EntityPlayer): Float =
  {
    if (energy != null)
    {
      map.put(LanguageUtility.getLocal("tooltip.energy") + ": " + new UnitDisplay(UnitDisplay.Unit.JOULES, energy.getEnergy), 0xFFFFFF)
    }
    if (tankInputDeuterium.getFluidAmount > 0)
    {
      map.put(LanguageUtility.getLocal("fluid.deuterium") + ": " + tankInputDeuterium.getFluidAmount + " L", 0xFFFFFF)
    }
    if (tankInputTritium.getFluidAmount > 0)
    {
      map.put(LanguageUtility.getLocal("fluid.tritium") + ": " + tankInputTritium.getFluidAmount + " L", 0xFFFFFF)
    }
    if (tankOutput.getFluidAmount > 0)
    {
      map.put(LanguageUtility.getLocal("fluid.plasma") + ": " + tankOutput.getFluidAmount + " L", 0xFFFFFF)
    }
    return 1.5f
  }

  def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
  {
    if (resource.isFluidEqual(AtomicContent.FLUIDSTACK_DEUTERIUM))
    {
      return tankInputDeuterium.fill(resource, doFill)
    }
    if (resource.isFluidEqual(AtomicContent.getFluidStackTritium))
    {
      return tankInputTritium.fill(resource, doFill)
    }
    return 0
  }

  def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack =
  {
    return drain(from, resource.amount, doDrain)
  }

  def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack =
  {
    return tankOutput.drain(maxDrain, doDrain)
  }

  def canFill(from: ForgeDirection, fluid: Fluid): Boolean =
  {
    return fluid.getID == AtomicContent.FLUID_DEUTERIUM.getID || fluid.getID == AtomicContent.getFluidTritium.getID
  }

  def canDrain(from: ForgeDirection, fluid: Fluid): Boolean =
  {
    return fluid eq AtomicContent.FLUID_PLASMA
  }

  def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] =
  {
    return Array[FluidTankInfo](tankInputDeuterium.getInfo, tankInputTritium.getInfo, tankOutput.getInfo)
  }

  override def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    return FluidUtility.playerActivatedFluidItem(world, xi, yi, zi, player, side)
  }

}
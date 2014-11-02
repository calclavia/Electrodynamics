package resonantinduction.archaic.firebox

import java.util.List

import cpw.mods.fml.relauncher.{Side, SideOnly}
import io.netty.buffer.ByteBuf
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.inventory.IInventory
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntityFurnace
import net.minecraft.util.IIcon
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._
import resonant.api.electric.EnergyStorage
import resonant.content.spatial.block.SpatialBlock
import resonant.engine.grid.thermal.{BoilEvent, ThermalPhysics}
import resonant.lib.content.prefab.TEnergyStorage
import resonant.lib.content.prefab.java.TileElectricInventory
import resonant.lib.network.Synced
import resonant.lib.network.discriminator.{PacketAnnotation, PacketType}
import resonant.lib.network.handle.IPacketReceiver
import resonant.lib.transform.vector.Vector3
import resonant.lib.utility.FluidUtility
import resonant.lib.wrapper.WrapList._
import resonantinduction.core.Reference

/**
 * Meant to replace the furnace class.
 *
 * @author Calclavia
 */
class TileFirebox extends TileElectricInventory(Material.rock) with IPacketReceiver with IFluidHandler with TEnergyStorage
{
  /**
   * 1KG of coal ~= 24MJ
   * Approximately one coal = 4MJ, one coal lasts 80 seconds. Therefore, we are producing 50000
   * watts.
   * The power of the firebox in terms of thermal energy. The thermal energy can be transfered
   * into fluids to increase their internal energy.
   */
  private final val POWER: Long = 100000
  protected var tank: FluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME)
  @Synced private var burnTime: Int = 0
  private var heatEnergy: Long = 0
  private var boiledVolume: Int = 0

  //TODO: Dummy
  energy = new EnergyStorage(0)
  energy.setCapacity(POWER)
  energy.setMaxTransfer((POWER * 2) / 20)
  setIO(ForgeDirection.UP, 0)

  override def update
  {
    if (!worldObj.isRemote)
    {
      val drainFluid: FluidStack = tank.drain(FluidContainerRegistry.BUCKET_VOLUME, false)
      if (drainFluid != null && drainFluid.amount == FluidContainerRegistry.BUCKET_VOLUME && drainFluid.fluidID == FluidRegistry.LAVA.getID)
      {
        if (burnTime == 0)
        {
          tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true)
          burnTime += 20000
          worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
        }
      }
      else if (isElectrical && energy.checkExtract)
      {
        energy.extractEnergy
        if (burnTime == 0)
        {
          worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
        }
        burnTime += 2
      }
      else if (canBurn(getStackInSlot(0)))
      {
        if (burnTime == 0)
        {
          burnTime += TileEntityFurnace.getItemBurnTime(this.getStackInSlot(0))
          decrStackSize(0, 1)
          worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
        }
      }
      val block: Block = worldObj.getBlock(xCoord, yCoord + 1, zCoord)
      if (burnTime > 0)
      {
        if (block == null)
        {
          worldObj.setBlock(xCoord, yCoord + 1, zCoord, Blocks.fire)
        }
        heatEnergy += POWER / 20
        var usedHeat: Boolean = false
        if (block eq Blocks.water)
        {
          usedHeat = true
          val volume: Int = 100
          if (heatEnergy >= getRequiredBoilWaterEnergy(volume))
          {
            if (FluidRegistry.getFluid("steam") != null)
            {
              MinecraftForge.EVENT_BUS.post(new BoilEvent(worldObj, asVectorWorld.add(0, 1, 0), new FluidStack(FluidRegistry.WATER, volume), new FluidStack(FluidRegistry.getFluid("steam"), volume), 2, false))
              boiledVolume += volume
            }
            if (boiledVolume >= FluidContainerRegistry.BUCKET_VOLUME)
            {
              boiledVolume = 0
              worldObj.setBlockToAir(xCoord, yCoord + 1, zCoord)
            }
            heatEnergy = 0
          }
        }
        if (!usedHeat)
        {
          heatEnergy = 0
        }
        if (({
          burnTime -= 1;
          burnTime
        }) == 0)
        {
          if (block eq Blocks.fire)
          {
            worldObj.setBlockToAir(xCoord, yCoord + 1, zCoord)
          }
          worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
        }
      }
    }
  }

  /**
   * Approximately 327600 + 2257000 = 2584600.
   *
   * @param volume
   * @return
   */
  def getRequiredBoilWaterEnergy(volume: Int): Long =
  {
    return ThermalPhysics.getRequiredBoilWaterEnergy(worldObj, xCoord, zCoord, volume).asInstanceOf[Long]
  }

  def getMeltIronEnergy(volume: Float): Long =
  {
    val temperatureChange: Float = 1811 - ThermalPhysics.getTemperatureForCoordinate(worldObj, xCoord, zCoord)
    val mass: Float = ThermalPhysics.getMass(volume, 7.9f)
    return (ThermalPhysics.getEnergyForTemperatureChange(mass, 450, temperatureChange) + ThermalPhysics.getEnergyForStateChange(mass, 272000)).asInstanceOf[Long]
  }

  def isElectrical: Boolean =
  {
    return this.getBlockMetadata == 1
  }

  def canBurn(stack: ItemStack): Boolean =
  {
    return TileEntityFurnace.getItemBurnTime(stack) > 0
  }

  def isBurning: Boolean =
  {
    return burnTime > 0
  }

  override def isItemValidForSlot(i: Int, itemStack: ItemStack): Boolean =
  {
    return i == 0 && canBurn(itemStack)
  }

  override def getDescPacket: PacketAnnotation =
  {
    return new PacketAnnotation(this)
  }

  def read(data: ByteBuf, player: EntityPlayer, `type`: PacketType)
  {
    this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord)
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    burnTime = nbt.getInteger("burnTime")
    tank.readFromNBT(nbt)
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setInteger("burnTime", burnTime)
    tank.writeToNBT(nbt)
  }

  def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
  {
    return tank.fill(resource, doFill)
  }

  def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack =
  {
    if (resource == null || resource.getFluid == FluidRegistry.LAVA)
    {
      return null
    }
    return tank.drain(resource.amount, doDrain)
  }

  def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack =
  {
    return tank.drain(maxDrain, doDrain)
  }

  def canFill(from: ForgeDirection, fluid: Fluid): Boolean =
  {
    return fluid eq FluidRegistry.LAVA
  }

  def canDrain(from: ForgeDirection, fluid: Fluid): Boolean =
  {
    return false
  }

  def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] =
  {
    return Array[FluidTankInfo](tank.getInfo)
  }

  @SideOnly(Side.CLIENT) override def registerIcons(iconReg: IIconRegister)
  {
    super.registerIcons(iconReg)
    SpatialBlock.icon.put("firebox_side_on", iconReg.registerIcon(Reference.prefix + "firebox_side_on"))
    SpatialBlock.icon.put("firebox_side_off", iconReg.registerIcon(Reference.prefix + "firebox_side_off"))
    SpatialBlock.icon.put("firebox_top_on", iconReg.registerIcon(Reference.prefix + "firebox_top_on"))
    SpatialBlock.icon.put("firebox_top_off", iconReg.registerIcon(Reference.prefix + "firebox_top_off"))
    SpatialBlock.icon.put("firebox_electric_side_on", iconReg.registerIcon(Reference.prefix + "firebox_electric_side_on"))
    SpatialBlock.icon.put("firebox_electric_side_off", iconReg.registerIcon(Reference.prefix + "firebox_electric_side_off"))
    SpatialBlock.icon.put("firebox_electric_top_on", iconReg.registerIcon(Reference.prefix + "firebox_electric_top_on"))
    SpatialBlock.icon.put("firebox_electric_top_off", iconReg.registerIcon(Reference.prefix + "firebox_electric_top_off"))
  }

  @SideOnly(Side.CLIENT) override def getIcon(side: Int, meta: Int): IIcon =
  {
    if (side == 0)
    {
      return SpatialBlock.icon.get("firebox")
    }
    val isElectric: Boolean = meta == 1
    val isBurning: Boolean = false
    if (side == 1)
    {
      return if (isBurning) (if (isElectric) SpatialBlock.icon.get("firebox_electric_top_on") else SpatialBlock.icon.get("firebox_top_on")) else (if (isElectric) SpatialBlock.icon.get("firebox_electric_top_off") else SpatialBlock.icon.get("firebox_top_off"))
    }
    return if (isBurning) (if (isElectric) SpatialBlock.icon.get("firebox_electric_side_on") else SpatialBlock.icon.get("firebox_side_on")) else (if (isElectric) SpatialBlock.icon.get("firebox_electric_side_off") else SpatialBlock.icon.get("firebox_side_off"))
  }

  override def click(player: EntityPlayer)
  {
    if (server)
    {
      extractItem(this.asInstanceOf[IInventory], 0, player)
    }
  }

  override def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (FluidUtility.playerActivatedFluidItem(world, xi, yi, zi, player, side))
    {
      return true
    }
    return interactCurrentItem(this.asInstanceOf[IInventory], 0, player)
  }

  override def getSubBlocks(par1: Item, par2CreativeTabs: CreativeTabs, par3List: List[_])
  {
    par3List.add(new ItemStack(par1, 1, 0))
    par3List.add(new ItemStack(par1, 1, 1))
  }

}
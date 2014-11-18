package resonantinduction.archaic.firebox

import java.util.List

import cpw.mods.fml.relauncher.{Side, SideOnly}
import io.netty.buffer.ByteBuf
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
import resonant.content.spatial.block.{SpatialBlock, SpatialTile}
import resonant.engine.grid.thermal.{BoilEvent, ThermalPhysics}
import resonant.lib.content.prefab.{TElectric, TInventory}
import resonant.lib.network.ByteBufWrapper._
import resonant.lib.network.discriminator.PacketType
import resonant.lib.network.handle.{TPacketReceiver, TPacketSender}
import resonant.lib.transform.vector.Vector3
import resonant.lib.utility.FluidUtility
import resonant.lib.wrapper.WrapList._
import resonantinduction.core.Reference

/**
 * Meant to replace the furnace class.
 *
 * @author Calclavia
 */
class TileFirebox extends SpatialTile(Material.rock) with IFluidHandler with TInventory with TElectric with TPacketSender with TPacketReceiver
{
  /**
   * 1KG of coal ~= 24MJ
   * Approximately one coal = 4MJ, one coal lasts 80 seconds. Therefore, we are producing 50000
   * watts.
   * The power of the firebox in terms of thermal energy. The thermal energy can be transfered
   * into fluids to increase their internal energy.
   */
  private final val power = 100000
  protected val tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME)
  private var burnTime = 0
  private var heatEnergy = 0d
  private var boiledVolume = 0

  val energy = new EnergyStorage(0)
  energy.setCapacity(power)
  energy.setMaxTransfer((power * 2) / 20)
  setIO(ForgeDirection.UP, 0)

  override def update()
  {
    super.update()

    if (!worldObj.isRemote)
    {
      val drainFluid = tank.drain(FluidContainerRegistry.BUCKET_VOLUME, false)

      if (drainFluid != null && drainFluid.amount == FluidContainerRegistry.BUCKET_VOLUME && drainFluid.fluidID == FluidRegistry.LAVA.getID)
      {
        if (burnTime == 0)
        {
          tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true)
          burnTime += 20000
          sendPacket(0)
        }
      }
      else if (isElectrical && energy.checkExtract)
      {
        energy.extractEnergy
        if (burnTime == 0)
        {
          sendPacket(0)
        }
        burnTime += 2
      }
      else if (canBurn(getStackInSlot(0)))
      {
        if (burnTime == 0)
        {
          burnTime += TileEntityFurnace.getItemBurnTime(this.getStackInSlot(0))
          decrStackSize(0, 1)
          sendPacket(0)
        }
      }

      val block = worldObj.getBlock(xCoord, yCoord + 1, zCoord)

      if (burnTime > 0)
      {
        if (block.isAir(world, xi, yi + 1, zi))
        {
          worldObj.setBlock(xCoord, yCoord + 1, zCoord, Blocks.fire)
        }
        heatEnergy += power / 20
        var usedHeat = false

        if (block == Blocks.water)
        {
          //Boil water
          usedHeat = true
          val volume = 100

          if (heatEnergy >= getRequiredBoilWaterEnergy(volume))
          {
            if (FluidRegistry.getFluid("steam") != null)
            {
              MinecraftForge.EVENT_BUS.post(new BoilEvent(worldObj, toVectorWorld.add(0, 1, 0), new FluidStack(FluidRegistry.WATER, volume), new FluidStack(FluidRegistry.getFluid("steam"), volume), 2, false))
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

        burnTime -= 1

        if (burnTime == 0)
        {
          if (block == Blocks.fire)
          {
            worldObj.setBlockToAir(xCoord, yCoord + 1, zCoord)
          }

          sendPacket(0)
        }
      }
    }
  }

  override def getSizeInventory = 1

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

  def isBurning: Boolean = burnTime > 0

  override def isItemValidForSlot(i: Int, itemStack: ItemStack): Boolean =
  {
    return i == 0 && canBurn(itemStack)
  }

  /**
   * Override this method
   * Be sure to super this method or manually write the ID into the packet when sending
   */
  override def write(buf: ByteBuf, id: Int)
  {
    super.write(buf, id)
    buf <<< burnTime
  }

  override def read(buf: ByteBuf, id: Int, packetType: PacketType)
  {
    super.read(buf, id, packetType)
    burnTime = buf.readInt()
    markRender()
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
    return fluid == FluidRegistry.LAVA
  }

  def canDrain(from: ForgeDirection, fluid: Fluid): Boolean =
  {
    return false
  }

  def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] =
  {
    return Array[FluidTankInfo](tank.getInfo)
  }

  @SideOnly(Side.CLIENT)
  override def registerIcons(iconReg: IIconRegister)
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

  @SideOnly(Side.CLIENT)
  override def getIcon(side: Int, meta: Int): IIcon =
  {
    if (side == 0)
      return SpatialBlock.icon.get("firebox")

    val isElectric = meta == 1

    if (side == 1)
      return if (isBurning) (if (isElectric) SpatialBlock.icon.get("firebox_electric_top_on") else SpatialBlock.icon.get("firebox_top_on")) else (if (isElectric) SpatialBlock.icon.get("firebox_electric_top_off") else SpatialBlock.icon.get("firebox_top_off"))

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
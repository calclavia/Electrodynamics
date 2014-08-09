package resonantinduction.archaic.firebox;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import resonant.engine.grid.thermal.BoilEvent;
import resonant.engine.grid.thermal.ThermalPhysics;
import resonant.lib.network.discriminator.PacketAnnotation;
import resonant.lib.network.discriminator.PacketTile;
import resonant.lib.network.discriminator.PacketType;
import resonant.lib.network.handle.IPacketReceiver;
import resonantinduction.archaic.Archaic;
import resonantinduction.archaic.fluid.gutter.TileGutter;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonant.lib.network.Synced;
import resonantinduction.core.CoreContent;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.resource.ResourceGenerator;
import resonantinduction.core.resource.TileMaterial;
import universalelectricity.core.transform.vector.Vector3;
import resonant.lib.content.prefab.java.TileElectricInventory;
import com.google.common.io.ByteArrayDataInput;

/**
 * Meant to replace the furnace class.
 *
 * @author Calclavia
 */
public class TileFirebox extends TileElectricInventory implements IPacketReceiver, IFluidHandler
{
	/**
	 * 1KG of coal ~= 24MJ
	 * Approximately one coal = 4MJ, one coal lasts 80 seconds. Therefore, we are producing 50000
	 * watts.
	 * The power of the firebox in terms of thermal energy. The thermal energy can be transfered
	 * into fluids to increase their internal energy.
	 */
	private final long POWER = 100000;
	protected FluidTank tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
	@Synced
	private int burnTime;
	private long heatEnergy = 0;
	private int boiledVolume;

	public TileFirebox()
	{
        super(Material.rock);
        setCapacity(POWER);
        setMaxTransfer((POWER * 2) / 20);
		setIO(ForgeDirection.UP, 0);
	}

	@Override
	public void update()
	{
		if (!worldObj.isRemote)
		{
			/**
			 * Extract fuel source for burn time.
			 */
			FluidStack drainFluid = tank.drain(FluidContainerRegistry.BUCKET_VOLUME, false);

			if (drainFluid != null && drainFluid.amount == FluidContainerRegistry.BUCKET_VOLUME && drainFluid.fluidID == FluidRegistry.LAVA.getID())
			{
				if (burnTime == 0)
				{
					tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
					burnTime += 20000;
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}
			}
			else if (isElectrical() && energy().checkExtract())
			{
				energy().extractEnergy();

				if (burnTime == 0)
				{
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}

				burnTime += 2;
			}
			else if (canBurn(getStackInSlot(0)))
			{
				if (burnTime == 0)
				{
					burnTime += TileEntityFurnace.getItemBurnTime(this.getStackInSlot(0));
					decrStackSize(0, 1);
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}
			}

			Block block = worldObj.getBlock(xCoord, yCoord + 1, zCoord);

			if (burnTime > 0)
			{
				if (block == null)
				{
					worldObj.setBlock(xCoord, yCoord + 1, zCoord, Blocks.fire);
				}

				/**
				 * Try to heat up and melt blocks above it.
				 */
				heatEnergy += POWER / 20;
				boolean usedHeat = false;

				if (block == CoreContent.blockDust()|| block == CoreContent.blockRefinedDust())
				{
					usedHeat = true;

					TileEntity dustTile = worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);

					if (dustTile instanceof TileMaterial)
					{
						String name = ((TileMaterial) dustTile).name();
						int meta = worldObj.getBlockMetadata(xCoord, yCoord + 1, zCoord);

						if (heatEnergy >= getMeltIronEnergy(((meta + 1) / 7f) * 1000))
						{
							int volumeMeta = block == CoreContent.blockRefinedDust() ? meta : meta / 2;

							worldObj.setBlock(xCoord, yCoord + 1, zCoord, ResourceGenerator.getMolten(name), volumeMeta, 3);

							TileEntity tile = worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);

							if (tile instanceof TileMaterial)
							{
								((TileMaterial) tile).name_$eq(name);
							}

							heatEnergy = 0;
						}
					}
				}
				else if (block == Blocks.water)
				{
					usedHeat = true;
					int volume = 100;

					if (heatEnergy >= getRequiredBoilWaterEnergy(volume))
					{
						if (FluidRegistry.getFluid("steam") != null)
						{
							MinecraftForge.EVENT_BUS.post(new BoilEvent(worldObj, new Vector3(this).add(0, 1, 0), new FluidStack(FluidRegistry.WATER, volume), new FluidStack(FluidRegistry.getFluid("steam"), volume), 2, false));
							boiledVolume += volume;
						}

						if (boiledVolume >= FluidContainerRegistry.BUCKET_VOLUME)
						{
							boiledVolume = 0;
							worldObj.setBlockToAir(xCoord, yCoord + 1, zCoord);
						}

						heatEnergy = 0;
					}
				}
				else if (block == Archaic.blockGutter())
				{
					TileEntity tileEntity = worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);

					if (tileEntity instanceof TileGutter)
					{
						usedHeat = true;
						int volume = Math.min(((TileGutter) tileEntity).getInternalTank().getFluidAmount(), 10);
						if (volume > 0 && heatEnergy >= getRequiredBoilWaterEnergy(volume))
						{
							if (FluidRegistry.getFluid("steam") != null)
							{
								MinecraftForge.EVENT_BUS.post(new BoilEvent(worldObj, new Vector3(this).add(0, 1, 0), new FluidStack(FluidRegistry.WATER, volume), new FluidStack(FluidRegistry.getFluid("steam"), volume), 2, false));
								((TileGutter) tileEntity).drain(ForgeDirection.DOWN, volume, true);
							}

							heatEnergy = 0;
						}
					}
				}

				if (!usedHeat)
				{
					heatEnergy = 0;
				}

				if (--burnTime == 0)
				{
					if (block == Blocks.fire)
					{
						worldObj.setBlockToAir(xCoord, yCoord + 1, zCoord);
					}

					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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
	public long getRequiredBoilWaterEnergy(int volume)
	{
		return (long) ThermalPhysics.getRequiredBoilWaterEnergy(worldObj, xCoord, zCoord, volume);
	}

	public long getMeltIronEnergy(float volume)
	{
		float temperatureChange = 1811 - ThermalPhysics.getTemperatureForCoordinate(worldObj, xCoord, zCoord);
		float mass = ThermalPhysics.getMass(volume, 7.9f);
		return (long) (ThermalPhysics.getEnergyForTemperatureChange(mass, 450, temperatureChange) + ThermalPhysics.getEnergyForStateChange(mass, 272000));
	}

	public boolean isElectrical()
	{
		return this.getBlockMetadata() == 1;
	}

	public boolean canBurn(ItemStack stack)
	{
		return TileEntityFurnace.getItemBurnTime(stack) > 0;
	}

	public boolean isBurning()
	{
		return burnTime > 0;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack)
	{
		return i == 0 && canBurn(itemStack);
	}

	@Override
	public PacketAnnotation getDescPacket()
	{
		return new PacketAnnotation(this);
	}

	@Override
	public void read(ByteBuf data, EntityPlayer player, PacketType type)
    {
		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		burnTime = nbt.getInteger("burnTime");
		tank.readFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("burnTime", burnTime);
		tank.writeToNBT(nbt);
	}

	/* IFluidHandler */
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		return tank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if (resource == null || resource.getFluid() == FluidRegistry.LAVA)
		{
			return null;
		}
		return tank.drain(resource.amount, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return tank.drain(maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return fluid == FluidRegistry.LAVA;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[] { tank.getInfo() };
	}
}

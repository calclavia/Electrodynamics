package resonantinduction.archaic.firebox;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.resource.ResourceGenerator;
import resonantinduction.core.resource.TileMaterial;
import universalelectricity.api.energy.EnergyStorageHandler;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.IPacketSender;
import calclavia.lib.network.Synced;
import calclavia.lib.prefab.tile.TileElectricalInventory;
import calclavia.lib.thermal.BoilEvent;

import com.google.common.io.ByteArrayDataInput;

/**
 * Meant to replace the furnace class.
 * 
 * @author Calclavia
 * 
 */
public class TileFirebox extends TileElectricalInventory implements IPacketReceiver, IFluidHandler
{
	/**
	 * One coal = 4MJ, one coal lasts 80 seconds. Therefore, we are producing 50000 watts.
	 * The power of the firebox in terms of thermal energy. The thermal energy can be transfered
	 * into fluids to increase their internal energy.
	 */
	private final long POWER = 50000;

	@Synced
	private int burnTime;

	/**
	 * 
	 * It takes 338260 J to boile water.
	 * TODO: Make desert faster, col biomes slower.
	 */
	private final long requiredBoilWaterEnergy = 338260;

	/**
	 * Requires about 6.6MJ of energy to melt iron.
	 */
	private final long requiredMeltIronEnergy = 4781700 + 1904000;
	private long heatEnergy = 0;

	public TileFirebox()
	{
		energy = new EnergyStorageHandler(POWER, (POWER * 2) / 20);
		setIO(ForgeDirection.UP, 0);
	}

	@Override
	public void updateEntity()
	{
		if (!worldObj.isRemote)
		{
			/**
			 * Extract fuel source for burn time.
			 */
			FluidStack drainFluid = tank.drain(FluidContainerRegistry.BUCKET_VOLUME, false);

			if (drainFluid != null && drainFluid.amount == FluidContainerRegistry.BUCKET_VOLUME)
			{
				if (burnTime == 0)
				{
					tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
					burnTime += 20000;
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}
			}
			else if (isElectrical() && energy.checkExtract())
			{
				energy.extractEnergy();

				if (burnTime == 0)
				{
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}

				burnTime += 2;
			}
			else if (canBurn(this.getStackInSlot(0)))
			{
				if (burnTime == 0)
				{
					burnTime += TileEntityFurnace.getItemBurnTime(this.getStackInSlot(0));
					decrStackSize(0, 1);
					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}
			}

			int blockID = worldObj.getBlockId(xCoord, yCoord + 1, zCoord);

			if (burnTime > 0)
			{
				if (blockID == 0 && blockID != Block.fire.blockID)
				{
					worldObj.setBlock(xCoord, yCoord + 1, zCoord, Block.fire.blockID);
				}

				/**
				 * Try to heat up and melt blocks above it.
				 */
				heatEnergy += POWER / 20;
				boolean usedHeat = false;

				if (blockID == ResonantInduction.blockDust.blockID)
				{
					usedHeat = true;

					if (heatEnergy >= requiredMeltIronEnergy)
					{
						TileEntity dustTile = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);

						if (dustTile instanceof TileMaterial)
						{
							String name = ((TileMaterial) dustTile).name;
							// TODO: Make refined dust yield more molten fluid than normal dust.
							worldObj.setBlock(xCoord, yCoord + 1, zCoord, ResonantInduction.blockFluidMaterials.get(ResourceGenerator.materialNames.indexOf(name)).blockID, worldObj.getBlockMetadata(xCoord, yCoord + 1, zCoord), 3);
							TileEntity tile = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);

							if (tile instanceof TileMaterial)
							{
								((TileMaterial) tile).name = name;
							}

							heatEnergy = 0;
						}
					}
				}
				else if (blockID == Block.waterStill.blockID)
				{
					usedHeat = true;
					if (heatEnergy >= requiredBoilWaterEnergy)
					{
						if (FluidRegistry.getFluid("steam") != null)
							MinecraftForge.EVENT_BUS.post(new BoilEvent(worldObj, new Vector3(this).translate(0, 1, 0), new FluidStack(FluidRegistry.WATER, FluidContainerRegistry.BUCKET_VOLUME), new FluidStack(FluidRegistry.getFluid("steam"), FluidContainerRegistry.BUCKET_VOLUME), 2));

						worldObj.setBlock(xCoord, yCoord + 1, zCoord, 0);
						heatEnergy = 0;
					}
				}

				if (!usedHeat)
				{
					heatEnergy = 0;
				}

				if (--burnTime == 0)
				{
					if (blockID == Block.fire.blockID)
					{
						worldObj.setBlock(xCoord, yCoord + 1, zCoord, 0);
					}

					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				}
			}
		}
	}

	/**
	 * TODO: Make biome sensitivity.
	 */
	public long getRequiredBoilWaterEnergy()
	{
		return requiredBoilWaterEnergy;
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return isElectrical() && super.canConnect(direction);
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
	public Packet getDescriptionPacket()
	{
		return ResonantInduction.PACKET_ANNOTATION.getPacket(this);
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord);
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

	protected FluidTank tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);

	/* IFluidHandler */
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		return tank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if (resource == null || !resource.isFluidEqual(tank.getFluid()))
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

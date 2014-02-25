package resonantinduction.archaic.gutter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.mechanical.fluid.IFluidNetwork;
import resonantinduction.api.mechanical.fluid.IFluidPipe;
import resonantinduction.core.prefab.fluid.PipeNetwork;
import resonantinduction.core.prefab.fluid.TileFluidNetwork;
import calclavia.lib.multiblock.fake.IBlockActivate;
import calclavia.lib.utility.WrenchUtility;

/** @author Darkguardsman */
public class TileGutter extends TileFluidNetwork implements IFluidPipe
{
	private int pressure;

	public TileGutter()
	{
		getInternalTank().setCapacity(FluidContainerRegistry.BUCKET_VOLUME);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!worldObj.isRemote)
		{

		}
	}

	@Override
	public void validateConnectionSide(TileEntity tileEntity, ForgeDirection side)
	{
		if (!this.worldObj.isRemote)
		{
			if (tileEntity instanceof IFluidPipe)
			{
				if (tileEntity instanceof TileGutter)
				{
					getNetwork().merge(((TileGutter) tileEntity).getNetwork());
					this.setRenderSide(side, true);
					connectedBlocks[side.ordinal()] = tileEntity;
				}
			}
			else if (tileEntity instanceof IFluidHandler)
			{
				this.setRenderSide(side, true);
				connectedBlocks[side.ordinal()] = tileEntity;
			}
		}
	}

	@Override
	public boolean canFlow()
	{
		return true;
	}

	@Override
	public IFluidNetwork getNetwork()
	{
		if (this.network == null)
		{
			this.network = new PipeNetwork();
			this.network.addConnector(this);
		}
		return this.network;
	}

	@Override
	public void setNetwork(IFluidNetwork network)
	{
		if (network instanceof PipeNetwork)
		{
			this.network = network;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		tank.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		tank.readFromNBT(nbt);
	}

	@Override
	public int getPressure(ForgeDirection dir)
	{
		return pressure;
	}

	@Override
	public void setPressure(int amount)
	{
		pressure = amount;
	}

	@Override
	public int getMaxFlowRate()
	{
		return 10;
	}

}

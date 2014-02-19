package resonantinduction.archaic.channel;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.mechanical.fluid.IFluidConnector;
import resonantinduction.api.mechanical.fluid.IFluidNetwork;
import resonantinduction.api.mechanical.fluid.IFluidPipe;
import resonantinduction.core.prefab.fluid.PipeNetwork;
import resonantinduction.core.prefab.fluid.TileFluidNetwork;
import resonantinduction.mechanical.fluid.tank.TileTank;
import calclavia.lib.multiblock.fake.IBlockActivate;
import calclavia.lib.utility.WrenchUtility;

/** @author Darkguardsman */
public class TileChannel extends TileFluidNetwork implements IBlockActivate, IFluidPipe
{
	private boolean isExtracting = false;

	public TileChannel()
	{
		this.getInternalTank().setCapacity(1 * FluidContainerRegistry.BUCKET_VOLUME);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!worldObj.isRemote)
		{
			if (isExtracting && getNetwork().getTank().getFluidAmount() < getNetwork().getTank().getCapacity())
			{
				for (int i = 0; i < this.getConnections().length; i++)
				{
					Object obj = this.getConnections()[i];

					if (obj instanceof IFluidHandler)
					{
						FluidStack drain = ((IFluidHandler) obj).drain(ForgeDirection.getOrientation(i).getOpposite(), getMaxFlowRate(), true);
						fill(null, drain, true);
					}
				}
			}
		}
	}

	@Override
	public boolean onActivated(EntityPlayer player)
	{
		if (WrenchUtility.isUsableWrench(player, player.getCurrentEquippedItem(), xCoord, yCoord, zCoord))
		{
			if (!this.worldObj.isRemote)
			{
				isExtracting = !isExtracting;
				player.addChatMessage("Pipe extraction mode: " + isExtracting);
				WrenchUtility.damageWrench(player, player.getCurrentEquippedItem(), xCoord, yCoord, zCoord);
			}
			return true;
		}

		return false;
	}

	@Override
	public void validateConnectionSide(TileEntity tileEntity, ForgeDirection side)
	{
		if (!this.worldObj.isRemote)
		{
			if (tileEntity instanceof IFluidPipe)
			{
				if (tileEntity instanceof TileChannel)
				{
					getNetwork().merge(((TileChannel) tileEntity).getNetwork());
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
		return !isExtracting;
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
		nbt.setBoolean("isExtracting", isExtracting);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		isExtracting = nbt.getBoolean("isExtracting");
	}

	@Override
	public int getPressureIn(ForgeDirection side)
	{
		return 0;
	}

	@Override
	public void onWrongPressure(ForgeDirection side, int pressure)
	{
		// TODO place fluid blocks into the world

	}

	@Override
	public int getMaxPressure()
	{
		return 0;
	}

	@Override
	public int getPressure()
	{
		return this.getNetwork().getPressure();
	}

	@Override
	public int getMaxFlowRate()
	{
		return 500;
	}
}

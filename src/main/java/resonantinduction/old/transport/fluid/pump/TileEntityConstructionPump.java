package resonantinduction.old.transport.fluid.pump;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.core.tilenetwork.ITileConnector;
import resonantinduction.old.api.fluid.IDrain;
import resonantinduction.old.api.fluid.INetworkPipe;
import resonantinduction.old.transport.fluid.network.NetworkFluidTiles;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorHelper;

import com.builtbroken.common.Pair;

public class TileEntityConstructionPump extends TileEntityStarterPump implements IFluidHandler, ITileConnector
{
	/* LIQUID FLOW CONNECTION SIDES */
	/** Internal tank for interaction but not real storage */
	private FluidTank fakeTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);

	List<IDrain> drainsUsed = new ArrayList<IDrain>();

	public TileEntityConstructionPump()
	{
		super(50, 50, 30);
	}

	/**
	 * Gets the facing direction
	 * 
	 * @param input true for input side, false for output side
	 * @return
	 */
	public ForgeDirection getFacing(boolean input)
	{

		return input ? this.getDirection() : this.getDirection().getOpposite();
	}

	@Override
	public Pair<World, Vector3> getDrainOrigin()
	{
		TileEntity inputTile = VectorHelper.getTileEntityFromSide(worldObj, new Vector3(this), getFacing(true));
		TileEntity outputTile = VectorHelper.getTileEntityFromSide(worldObj, new Vector3(this), getFacing(false));
		IDrain drain = this.getNextDrain(inputTile, outputTile, drainsUsed);
		if (drain == null)
		{
			this.drainsUsed.clear();
			drain = this.getNextDrain(inputTile, outputTile, drainsUsed);
		}

		if (drain instanceof TileEntity)
		{
			this.drainsUsed.add(drain);
			return new Pair<World, Vector3>(((TileEntity) drain).worldObj, new Vector3(((TileEntity) drain)));
		}
		return null;
	}

	/**
	 * Gets the nextDrain in the list
	 * 
	 * @param inputTile - input tile must be an instance of INetworkPipe
	 * @param outputTile - output tile must be an instance of IFluidHandler
	 * @param ignoreList - list of drains to ignore so that the next one is selected
	 * @return the next drain it finds or null if it went threw the entire list. Its suggested to
	 * clear the ignoreList after getting null
	 */
	public IDrain getNextDrain(TileEntity inputTile, TileEntity outputTile, List<IDrain> ignoreList)
	{
		IDrain drain = null;
		if (ignoreList == null)
		{
			ignoreList = new ArrayList<IDrain>();
		}

		if (inputTile instanceof INetworkPipe && ((INetworkPipe) inputTile).getTileNetwork() instanceof NetworkFluidTiles)
		{
			if (outputTile instanceof IFluidHandler)
			{
				for (Entry<IFluidHandler, EnumSet<ForgeDirection>> entry : ((NetworkFluidTiles) ((INetworkPipe) inputTile).getTileNetwork()).connctedFluidHandlers.entrySet())
				{

					if (entry.getKey() instanceof IDrain && !ignoreList.contains(entry.getKey()))
					{
						drain = (IDrain) entry.getKey();
						break;
					}
				}
			}
		}
		return drain;
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return direction != getFacing(true) && direction != getFacing(false);
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		TileEntity entity = VectorHelper.getTileEntityFromSide(this.worldObj, new Vector3(this), getFacing(false));
		if (entity instanceof IFluidHandler)
		{
			return ((IFluidHandler) entity).fill(getFacing(false).getOpposite(), resource, doFill);
		}
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection direction)
	{
		if (direction == this.getFacing(false) && this.fakeTank != null)
		{
			return new FluidTankInfo[] { new FluidTankInfo(fakeTank.getFluid(), fakeTank.getCapacity()) };
		}
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return from != getFacing(true);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public boolean canTileConnect(Connection type, ForgeDirection dir)
	{
		TileEntity entity = new Vector3(this).modifyPositionFromSide(dir).getTileEntity(this.worldObj);
		return entity instanceof IFluidHandler && (dir == this.getFacing(false) || dir == this.getFacing(true));
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
	}

	@Override
	public String getMeterReading(EntityPlayer user, ForgeDirection side, EnumTools tool)
	{
		if (tool == EnumTools.PIPE_GUAGE)
		{
			TileEntity inputTile = VectorHelper.getTileEntityFromSide(worldObj, new Vector3(this), getFacing(true));
			if (inputTile instanceof INetworkPipe && ((INetworkPipe) inputTile).getTileNetwork() instanceof NetworkFluidTiles)
			{
				int count = 0;
				for (Entry<IFluidHandler, EnumSet<ForgeDirection>> entry : ((NetworkFluidTiles) ((INetworkPipe) inputTile).getTileNetwork()).connctedFluidHandlers.entrySet())
				{
					if (entry.getKey() instanceof IDrain)
					{
						count++;
					}
				}
				return "Drains conencted to input : " + count;
			}
		}
		return null;
	}

}

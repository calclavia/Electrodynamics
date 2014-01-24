package resonantinduction.mechanical.fluid.pump;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.fluid.IDrain;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.tile.TileAdvanced;
import calclavia.lib.utility.FluidUtility;

import com.builtbroken.common.Pair;

public class TileGrate extends TileAdvanced implements IFluidHandler, IDrain
{
	/* MAX BLOCKS DRAINED PER 1/2 SECOND */
	public static int MAX_WORLD_EDITS_PER_PROCESS = 50;
	private int currentWorldEdits = 0;

	/* LIST OF PUMPS AND THERE REQUESTS FOR THIS DRAIN */
	private HashMap<TileEntity, Pair<FluidStack, Integer>> requestMap = new HashMap<TileEntity, Pair<FluidStack, Integer>>();

	private List<Vector3> updateQue = new ArrayList<Vector3>();
	private LiquidPathFinder pathDrain;
	private LiquidPathFinder pathFill;

	private Vector3 lastDrainOrigin;
	public boolean markDrain = false;

	public LiquidPathFinder getFillFinder()
	{
		if (pathFill == null)
		{
			pathFill = new LiquidPathFinder(this.worldObj, 100, 100);
		}
		return pathFill;
	}

	@Override
	public Set<Vector3> getFillList()
	{
		return this.getFillFinder().refresh().results;
	}

	public LiquidPathFinder getLiquidFinder()
	{
		if (pathDrain == null)
		{
			pathDrain = new LiquidPathFinder(this.worldObj, 1000, 100);
		}
		return pathDrain;
	}

	@Override
	public Set<Vector3> getFluidList()
	{
		return this.getLiquidFinder().refresh().results;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		/* MAIN LOGIC PATH FOR DRAINING BODIES OF LIQUID */
		if (!this.worldObj.isRemote && this.ticks % 20 == 0 && !this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
		{
			this.currentWorldEdits = 0;

			/* ONLY FIND NEW SOURCES IF OUR CURRENT LIST RUNS DRY */
			if (this.getLiquidFinder().results.size() < TileGrate.MAX_WORLD_EDITS_PER_PROCESS + 10)
			{
				this.getLiquidFinder().refresh().start(new Vector3(this).translate(this.getDirection()), TileGrate.MAX_WORLD_EDITS_PER_PROCESS, false);
			}

			if (this.getFillFinder().results.size() < TileGrate.MAX_WORLD_EDITS_PER_PROCESS + 10)
			{
				this.getFillFinder().refresh().start(new Vector3(this).translate(this.getDirection()), TileGrate.MAX_WORLD_EDITS_PER_PROCESS, true);
			}
			/**
			 * If we can drain, do the drain action.
			 */
			if (markDrain)
			{
				drainAroundArea(worldObj, new Vector3(this), 3);
				markDrain = false;
			}
		}
	}

	/**
	 * Drains an area starting at the given location
	 * 
	 * @param world - world to drain in, most cases will be the TileEntities world
	 * @param loc - origin to start the path finder with. If this is an instance of IDrain this
	 * method will act different
	 */
	public void drainAroundArea(World world, Vector3 vec, int update)
	{
		Vector3 origin = vec.clone();
		if (origin == null)
		{
			return;
		}

		/* Update last drain origin to prevent failed path finding */
		if (this.lastDrainOrigin == null || !this.lastDrainOrigin.equals(origin))
		{
			this.lastDrainOrigin = origin.clone();
			this.getLiquidFinder().reset();
		}

		TileEntity drain = vec.clone().getTileEntity(world);
		TileEntity originTile = null;

		Set<Vector3> drainList = null;

		if (drain instanceof IDrain)
		{
			if (!((IDrain) drain).canDrain(((IDrain) drain).getDirection()))
			{
				return;
			}
			origin = vec.translate(((IDrain) drain).getDirection());
			originTile = origin.getTileEntity(world);

			if (originTile instanceof IFluidHandler)
			{
				FluidStack draStack = ((IFluidHandler) originTile).drain(ForgeDirection.UP, MAX_WORLD_EDITS_PER_PROCESS * FluidContainerRegistry.BUCKET_VOLUME, false);

				if (draStack != null && FluidUtility.fillTanksAllSides(worldObj, new Vector3(this), draStack, false, ForgeDirection.DOWN) > 0)
				{
					((IFluidHandler) originTile).drain(ForgeDirection.UP, FluidUtility.fillTanksAllSides(worldObj, new Vector3(this), draStack, true, ForgeDirection.DOWN), true);

				}
			}
			else
			{
				drainList = ((IDrain) drain).getFluidList();
			}
		}

		if (drainList == null)
		{
			if (this.getLiquidFinder().results.size() < MAX_WORLD_EDITS_PER_PROCESS + 10)
			{
				this.getLiquidFinder().setWorld(world).refresh().start(origin, MAX_WORLD_EDITS_PER_PROCESS, false);
			}
			drainList = this.getLiquidFinder().refresh().results;
		}

		if (originTile == null && drainList != null && drainList.size() > 0)
		{
			Iterator<Vector3> fluidList = drainList.iterator();

			while (fluidList.hasNext())
			{
				if (this.currentWorldEdits >= MAX_WORLD_EDITS_PER_PROCESS)
				{
					break;
				}

				Vector3 drainLocation = fluidList.next();
				FluidStack drainStack = FluidUtility.drainBlock(world, drainLocation, false, 3);
				int filled = FluidUtility.fillTanksAllSides(worldObj, new Vector3(this), drainStack, false);

				if (drainStack != null && filled >= drainStack.amount)
				{
					/* Remove the block that we drained. */
					FluidUtility.drainBlock(this.worldObj, drainLocation, true, update);
					FluidUtility.fillTanksAllSides(worldObj, new Vector3(this), drainStack, true);
					this.currentWorldEdits++;
					fluidList.remove();

					if (drain instanceof IDrain)
					{
						((IDrain) drain).onUse(drainLocation);
					}
				}
			}
		}
	}

	/**
	 * Fills the area with fluid.
	 * 
	 * @return Amount filled
	 */
	public int fillArea(FluidStack resource, boolean doFill)
	{
		int fillVolume = 0;

		if (this.currentWorldEdits < MAX_WORLD_EDITS_PER_PROCESS)
		{
			/* ID LIQUID BLOCK AND SET VARS FOR BLOCK PLACEMENT */
			if (resource == null || resource.amount < FluidContainerRegistry.BUCKET_VOLUME)
			{
				return 0;
			}

			fillVolume = resource.amount;

			List<Vector3> fluids = new ArrayList<Vector3>();
			List<Vector3> blocks = new ArrayList<Vector3>();
			List<Vector3> filled = new ArrayList<Vector3>();
			/* Sort results out into two groups and clear the rest out of the result list */
			Iterator<Vector3> it = this.getFillFinder().refresh().results.iterator();
			while (it.hasNext())
			{
				Vector3 vec = it.next();
				if (FluidUtility.isFillableFluid(worldObj, vec) && !fluids.contains(vec) && !blocks.contains(vec))
				{
					fluids.add(vec);
				}
				else if (FluidUtility.isFillableBlock(worldObj, vec) && !blocks.contains(vec) && !fluids.contains(vec))
				{
					blocks.add(vec);
				}
				else
				{
					it.remove();
				}
			}
			/* Fill non-full fluids first */
			for (Vector3 loc : fluids)
			{
				if (fillVolume <= 0)
				{
					break;
				}
				if (FluidUtility.isFillableFluid(worldObj, loc))
				{

					fillVolume -= FluidUtility.fillBlock(worldObj, loc, FluidUtility.getStack(resource, fillVolume), doFill);

					if (doFill)
					{
						filled.add(loc);
						this.currentWorldEdits++;
						if (!this.updateQue.contains(loc))
						{
							this.updateQue.add(loc);
						}
					}

				}

			}
			/* Fill air or replaceable blocks after non-full fluids */
			for (Vector3 loc : blocks)
			{
				if (fillVolume <= 0)
				{
					break;
				}
				if (FluidUtility.isFillableBlock(worldObj, loc))
				{
					fillVolume -= FluidUtility.fillBlock(worldObj, loc, FluidUtility.getStack(resource, fillVolume), doFill);

					if (doFill)
					{
						filled.add(loc);
						this.currentWorldEdits++;
						if (!this.updateQue.contains(loc))
						{
							this.updateQue.add(loc);
						}
					}

				}
			}
			this.getLiquidFinder().results.removeAll(filled);
			return Math.max(resource.amount - fillVolume, 0);
		}
		return 0;
	}

	@Override
	public ForgeDirection getDirection()
	{
		int meta = 0;
		if (worldObj != null)
		{
			meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord) % 6;
		}
		return ForgeDirection.getOrientation(meta);
	}

	@Override
	public void setDirection(ForgeDirection direction)
	{
		if (direction != null && direction != this.getDirection())
		{
			this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, direction.ordinal(), 3);
		}
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return this.getDirection() != from;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if (resource == null)
		{
			return 0;
		}

		return this.fillArea(resource, doFill);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if (resource != null)
			return drain(from, resource.amount, doDrain);
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		markDrain = true;
		return null;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[] { new FluidTank(this.getLiquidFinder().results.size() * FluidContainerRegistry.BUCKET_VOLUME).getInfo() };
	}

	@Override
	public boolean canDrain(ForgeDirection direction)
	{
		return direction == this.getDirection() && !this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
	}

	@Override
	public boolean canFill(ForgeDirection direction)
	{
		return direction == this.getDirection() && !this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
	}

	@Override
	public void onUse(Vector3 vec)
	{
		this.currentWorldEdits++;
	}

}

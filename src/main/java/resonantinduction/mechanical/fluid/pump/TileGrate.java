package resonantinduction.mechanical.fluid.pump;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.fluid.IDrain;
import resonantinduction.mechanical.fluid.prefab.TileEntityFluidDevice;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.FluidUtility;

import com.builtbroken.common.Pair;

public class TileGrate extends TileEntityFluidDevice implements IFluidHandler, IDrain
{
	/* MAX BLOCKS DRAINED PER 1/2 SECOND */
	public static int MAX_WORLD_EDITS_PER_PROCESS = 50;
	private int currentWorldEdits = 0;

	/* LIST OF PUMPS AND THERE REQUESTS FOR THIS DRAIN */
	private HashMap<TileEntity, Pair<FluidStack, Integer>> requestMap = new HashMap<TileEntity, Pair<FluidStack, Integer>>();

	private List<Vector3> updateQue = new ArrayList<Vector3>();
	private LiquidPathFinder pathDrain;
	private LiquidPathFinder pathFill;

	public boolean canDrain()
	{
		return this.getBlockMetadata() < 6;
	}

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
				this.getLiquidFinder().refresh().start(new Vector3(this).modifyPositionFromSide(this.getDirection()), TileGrate.MAX_WORLD_EDITS_PER_PROCESS, false);
			}

			if (this.getFillFinder().results.size() < TileGrate.MAX_WORLD_EDITS_PER_PROCESS + 10)
			{
				this.getFillFinder().refresh().start(new Vector3(this).modifyPositionFromSide(this.getDirection()), TileGrate.MAX_WORLD_EDITS_PER_PROCESS, true);
			}

		}
	}

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

			// System.out.println("Drain>>FillArea>>Targets>> " + getFillFinder().results.size());

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
					// System.out.println("Drain>>FillArea>>Filling>>" + (doFill ? "" : "Sim>>") +
					// ">>Fluid>" + loc.toString());

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
					System.out.println("Drain>>FillArea>>Filling>>" + (doFill ? "" : "Sim>>") + ">>Block>" + loc.toString());

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
			// System.out.println("Drain>>FillArea>>Filling>>Filled>>" + (doFill ? "" : "Sim>>") +
			// (resource.amount - fillVolume) + "mb");
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
		return this.getDirection() != from && !this.canDrain();
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if (resource == null || this.canDrain())
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
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
		return direction == this.getDirection() && !this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord) && this.canDrain();
	}

	@Override
	public boolean canFill(ForgeDirection direction)
	{
		return direction == this.getDirection() && !this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord) && !this.canDrain();
	}

	@Override
	public void onUse(Vector3 vec)
	{
		this.currentWorldEdits++;
	}
}

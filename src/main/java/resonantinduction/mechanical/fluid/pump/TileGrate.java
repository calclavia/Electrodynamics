package resonantinduction.mechanical.fluid.pump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.fluid.IDrain;
import universalelectricity.api.net.IUpdate;
import universalelectricity.api.vector.Vector3;
import universalelectricity.core.net.NetworkTickHandler;
import calclavia.lib.prefab.tile.TileAdvanced;
import calclavia.lib.utility.FluidUtility;

public class TileGrate extends TileAdvanced implements IFluidHandler, IDrain
{
	public static final int MAX_FLUID_MODIFY_RATE = 50;

	private long lastUseTime = 0;
	private int currentWorldEdits = 0;

	private LiquidPathFinder pathDrain;
	private LiquidPathFinder pathFill;

	public LiquidPathFinder getFillFinder()
	{
		if (pathFill == null)
		{
			pathFill = new LiquidPathFinder(this.worldObj, 100, 100);
		}
		return pathFill;
	}

	@Override
	public List<Vector3> getFillList()
	{
		return this.getFillFinder().refresh().sortedResults;
	}

	public LiquidPathFinder getDrainFinder()
	{
		if (pathDrain == null)
		{
			pathDrain = new LiquidPathFinder(this.worldObj, 1000, 100);
		}
		return pathDrain;
	}

	@Override
	public List<Vector3> getDrainList()
	{
		return getDrainFinder().refresh().sortedResults;
	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	public void update()
	{
		if (System.currentTimeMillis() - lastUseTime > 1000)
		{
			currentWorldEdits = 0;
			lastUseTime = System.currentTimeMillis();
		}
	}

	/**
	 * Updates the pathfinding operation.
	 */
	public void doPathfinding()
	{
		if (this.getDrainFinder().results.size() < TileGrate.MAX_FLUID_MODIFY_RATE + 10)
		{
			this.getDrainFinder().refresh().start(new Vector3(this).translate(this.getDirection()), TileGrate.MAX_FLUID_MODIFY_RATE, false);
		}

		if (this.getFillFinder().results.size() < TileGrate.MAX_FLUID_MODIFY_RATE + 10)
		{
			this.getFillFinder().refresh().start(new Vector3(this).translate(this.getDirection()), TileGrate.MAX_FLUID_MODIFY_RATE, true);
		}
	}

	@Override
	public ForgeDirection getDirection()
	{
		return ForgeDirection.getOrientation(worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
	}

	@Override
	public void setDirection(ForgeDirection direction)
	{
		this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, direction.ordinal(), 3);
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		update();

		if (resource == null)
		{
			return 0;
		}
		if (currentWorldEdits < MAX_FLUID_MODIFY_RATE)
		{
			int remainingVolume = resource.amount;

			/* ID LIQUID BLOCK AND SET VARS FOR BLOCK PLACEMENT */
			if (resource == null || resource.amount < FluidContainerRegistry.BUCKET_VOLUME)
			{
				return 0;
			}

			List<Vector3> fluids = new ArrayList<Vector3>();
			List<Vector3> blocks = new ArrayList<Vector3>();
			List<Vector3> filled = new ArrayList<Vector3>();

			if (getFillList() == null || getFillList().size() == 0)
			{
				doPathfinding();
			}

			/* Sort results out into two groups and clear the rest out of the result list */
			Iterator<Vector3> it = getFillList().iterator();

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
				if (remainingVolume <= 0)
				{
					break;
				}

				if (FluidUtility.isFillableFluid(worldObj, loc))
				{
					remainingVolume -= FluidUtility.fillBlock(worldObj, loc, FluidUtility.getStack(resource, remainingVolume), doFill);

					if (doFill)
					{
						filled.add(loc);
						currentWorldEdits++;
					}
				}
			}

			/* Fill air or replaceable blocks after non-full fluids */
			for (Vector3 loc : blocks)
			{
				if (remainingVolume <= 0)
				{
					break;
				}

				if (FluidUtility.isFillableBlock(worldObj, loc))
				{
					remainingVolume -= FluidUtility.fillBlock(worldObj, loc, FluidUtility.getStack(resource, remainingVolume), doFill);

					if (doFill)
					{
						filled.add(loc);
						currentWorldEdits++;
					}
				}
			}

			this.getDrainFinder().results.removeAll(filled);
			return Math.max(resource.amount - remainingVolume, 0);
		}

		return 0;
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
		update();

		FluidStack resultStack = null;

		if (getDrainList() == null || getDrainList().size() == 0)
		{
			doPathfinding();
		}

		List<Vector3> drainList = getDrainList();

		if (drainList != null && drainList.size() > 0)
		{
			Iterator<Vector3> iterator = drainList.iterator();

			while (iterator.hasNext())
			{
				if (currentWorldEdits >= MAX_FLUID_MODIFY_RATE)
				{
					break;
				}

				final Vector3 drainLocation = iterator.next();
				FluidStack drainStack = FluidUtility.drainBlock(worldObj, drainLocation, false, 3);

				if (resultStack == null)
				{
					drainStack = FluidUtility.drainBlock(worldObj, drainLocation, doDrain, 2);
					resultStack = drainStack;
				}
				else if (resultStack.equals(drainStack))
				{
					drainStack = FluidUtility.drainBlock(worldObj, drainLocation, doDrain, 2);
					resultStack.amount += drainStack.amount;
				}

				if (doDrain)
				{
					/**
					 * Add a delayed notify event to prevent infinite fluids from reconstructing
					 * quickly.
					 */
					NetworkTickHandler.addNetwork(new IUpdate()
					{
						int wait = 20;

						@Override
						public void update()
						{
							if (--wait <= 0)
							{
								worldObj.notifyBlocksOfNeighborChange(drainLocation.intX(), drainLocation.intY(), drainLocation.intZ(), worldObj.getBlockId(drainLocation.intX(), drainLocation.intY(), drainLocation.intZ()), 20);
							}
						}

						@Override
						public boolean canUpdate()
						{
							return true;
						}

						@Override
						public boolean continueUpdate()
						{
							return wait > 0;
						}
					});
				}

				currentWorldEdits++;
				iterator.remove();

				if (resultStack.amount >= maxDrain)
				{
					break;
				}
			}
		}

		return resultStack;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[] { new FluidTank(this.getDrainFinder().results.size() * FluidContainerRegistry.BUCKET_VOLUME).getInfo() };
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return getDirection() != from;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return getDirection() != from;
	}
}

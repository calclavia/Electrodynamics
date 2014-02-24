package resonantinduction.mechanical.fluid.transport;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.tile.TileAdvanced;
import calclavia.lib.utility.FluidUtility;

public class TileGrate extends TileAdvanced implements IFluidHandler
{
	private GratePathfinder gratePath;

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public ForgeDirection getDirection()
	{
		return ForgeDirection.getOrientation(getBlockMetadata());
	}

	@Override
	public void setDirection(ForgeDirection direction)
	{
		this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, direction.ordinal(), 3);
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return null;
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

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if (resource != null && resource.amount > 0)
		{
			if (gratePath == null)
			{
				gratePath = new GratePathfinder(true);
				gratePath.startFill(new Vector3(this), resource.fluidID);
			}

			return gratePath.tryFill(resource.amount, 2000);
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
		if (maxDrain > 0)
		{
			if (gratePath == null)
			{
				gratePath = new GratePathfinder(false);

				if (!gratePath.startDrain(new Vector3(this)))
				{
					resetPath();
				}
			}

			if (gratePath != null && gratePath.tryPopulateDrainMap(500))
			{
				return gratePath.tryDrain(maxDrain, doDrain);
			}
		}

		return null;
	}

	/**
	 * Pathfinding operations
	 * 
	 * @author Calclavia
	 */
	public void resetPath()
	{
		this.gratePath = null;
	}

	public class GratePathfinder
	{
		/**
		 * The starting vector for our grate.
		 */
		Vector3 start;

		/**
		 * All the back trace blocks tracing back to the original.
		 */
		HashMap<Vector3, Vector3> navigationMap = new HashMap<Vector3, Vector3>();

		/**
		 * The nodes we're currently working on.
		 */
		PriorityQueue<ComparableVector> workingNodes;

		/**
		 * The list of blocks to drain.
		 */
		PriorityQueue<ComparableVector> drainNodes = new PriorityQueue<ComparableVector>(1024, Collections.reverseOrder());

		/**
		 * The type of fluid we're dealing with. When draining, this will be the type of fluid being
		 * drained.
		 */
		public Fluid fluidType;

		public GratePathfinder(boolean checkVertical)
		{
			if (checkVertical)
			{
				this.workingNodes = new PriorityQueue<ComparableVector>();
			}
			else
			{
				this.workingNodes = new PriorityQueue<ComparableVector>(1024, new Comparator()
				{

					@Override
					public int compare(Object a, Object b)
					{
						TileGrate.ComparableVector wa = (TileGrate.ComparableVector) a;
						TileGrate.ComparableVector wb = (TileGrate.ComparableVector) b;

						return wa.iterations - wb.iterations;
					}
				});
			}
		}

		/**
		 * Traces back to the start position to see if this fluid position is connected with the
		 * starting position through fluid mediums.
		 */
		public boolean isConnected(Vector3 check)
		{
			if (check.equals(this.start))
				return true;
			do
			{
				check = this.navigationMap.get(check);

				if (check == null)
					return false;

				if (check.equals(this.start))
					return true;
			}
			while (FluidUtility.getFluidFromBlock(TileGrate.this.worldObj, check) != null && fluidType.getID() == FluidUtility.getFluidFromBlock(TileGrate.this.worldObj, check).getID());

			return false;
		}

		public void startFill(Vector3 start, int fluidID)
		{
			this.fluidType = FluidRegistry.getFluid(fluidID);
			this.start = start;

			for (int i = 0; i < 6; i++)
			{
				ForgeDirection dir = ForgeDirection.getOrientation(i);

				if (dir == TileGrate.this.getDirection())
				{
					Vector3 check = start.clone().translate(dir);
					this.navigationMap.put(check, start);
					this.workingNodes.add(new TileGrate.ComparableVector(check, 0));
				}
			}
		}

		/**
		 * Tries to fill.
		 * 
		 * @param amount
		 * @param tries
		 * @return Amount filled.
		 */
		public int tryFill(int amount, int tries)
		{
			for (int i = 0; i < tries; i++)
			{
				ComparableVector next = this.workingNodes.poll();

				if (next == null)
				{
					TileGrate.this.resetPath();
					return 0;
				}

				if (!isConnected(next.position))
				{
					TileGrate.this.resetPath();
					return 0;
				}

				int filled = FluidUtility.fillBlock(TileGrate.this.worldObj, next.position, new FluidStack(fluidType, amount), true);
				amount -= filled;

				if (filled > 0)
				{
					addNextFill(next);
					return filled;
				}
			}

			return 0;
		}

		/**
		 * Adds new nodes into the map.
		 * 
		 * @param next
		 */
		public void addNextFill(ComparableVector next)
		{
			for (int i = 0; i < 6; i++)
			{
				Vector3 newPosition = next.position.clone().translate(ForgeDirection.getOrientation(i));

				if (!this.navigationMap.containsKey(newPosition))
				{
					this.navigationMap.put(newPosition, next.position);
					this.workingNodes.add(new ComparableVector(newPosition, next.iterations + 1));
				}
			}
		}

		public boolean startDrain(Vector3 start)
		{
			fluidType = null;
			this.start = start;

			for (int i = 0; i < 6; i++)
			{
				ForgeDirection dir = ForgeDirection.getOrientation(i);

				if (dir == TileGrate.this.getDirection())
				{
					Vector3 check = start.clone().translate(dir);
					this.navigationMap.put(check, start);
					this.workingNodes.add(new ComparableVector(check, 0));
					fluidType = FluidUtility.getFluidFromBlock(TileGrate.this.worldObj, check);
				}
			}

			return fluidType != null;
		}

		/**
		 * Creates a map of all the fluids.
		 * 
		 * @param tries
		 * @return
		 */
		public boolean tryPopulateDrainMap(int tries)
		{
			if (drainNodes.size() >= Integer.MAX_VALUE)
			{
				return true;
			}

			for (int i = 0; i < tries; i++)
			{
				ComparableVector check = workingNodes.poll();

				if (check == null)
					return true;

				Fluid checkFluid = FluidUtility.getFluidFromBlock(TileGrate.this.worldObj, check.position);

				if (checkFluid != null && fluidType.getID() == checkFluid.getID())
				{
					addNextDrain(check);

					int checkAmount = FluidUtility.getFluidAmountFromBlock(TileGrate.this.worldObj, check.position);

					if (checkAmount > 0)
						this.drainNodes.add(check);
				}
			}

			return false;
		}

		public void addNextDrain(ComparableVector next)
		{
			for (int i = 0; i < 6; i++)
			{
				Vector3 check = next.position.clone().translate(ForgeDirection.getOrientation(i));
				Fluid checkFluid = FluidUtility.getFluidFromBlock(TileGrate.this.worldObj, check);

				if (checkFluid != null && fluidType.getID() == checkFluid.getID())
				{
					if (!navigationMap.containsKey(check))
					{
						navigationMap.put(check, next.position);
						workingNodes.add(new TileGrate.ComparableVector(check, next.iterations + 1));
					}
				}
			}
		}

		/**
		 * Tries to drain a specific amount of fluid.
		 * 
		 * @return - The amount drained.
		 */
		public FluidStack tryDrain(int amount, boolean doDrain)
		{
			int drainedAmount = 0;

			while (!drainNodes.isEmpty())
			{
				ComparableVector fluidCoord = drainNodes.peek();

				if (!isConnected(fluidCoord.position))
				{
					TileGrate.this.resetPath();
					return new FluidStack(fluidType, drainedAmount);
				}

				if (FluidUtility.getFluidFromBlock(TileGrate.this.worldObj, fluidCoord.position) == null || this.fluidType.getID() != FluidUtility.getFluidFromBlock(TileGrate.this.worldObj, fluidCoord.position).getID())
				{
					this.drainNodes.poll();
				}
				else
				{
					int checkAmount = FluidUtility.getFluidAmountFromBlock(TileGrate.this.worldObj, fluidCoord.position);

					if (checkAmount == 0)
					{
						this.drainNodes.poll();
					}
					else
					{
						FluidStack fluidStack = FluidUtility.drainBlock(TileGrate.this.worldObj, fluidCoord.position, doDrain, 3);

						this.drainNodes.poll();

						if (fluidStack != null)
						{
							drainedAmount += fluidStack.amount;

							if (drainedAmount > amount)
							{
								return new FluidStack(fluidType, drainedAmount);
							}
						}
					}
				}
			}

			TileGrate.this.resetPath();
			return null;
		}

	}

	public static class ComparableVector implements Comparable
	{
		public Vector3 position;
		public int iterations;

		public ComparableVector(Vector3 position, int iterations)
		{
			this.position = position;
			this.iterations = iterations;
		}

		public int compareTo(Object obj)
		{
			ComparableVector wr = (ComparableVector) obj;
			if (this.position.y == wr.position.y)
				return this.iterations - wr.iterations;
			return this.position.intY() - wr.position.intY();
		}
	}
}

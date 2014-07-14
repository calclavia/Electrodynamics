package resonantinduction.archaic.fluid.grate;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import resonant.api.IRotatable;
import resonant.lib.config.Config;
import resonant.lib.utility.FluidUtility;
import resonantinduction.core.Reference;
import resonantinduction.core.grid.fluid.FluidPressureNode;
import resonantinduction.core.grid.fluid.TilePressureNode;
import universalelectricity.api.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileGrate extends TilePressureNode implements IRotatable
{
	@Config(comment = "The multiplier for the influence of the grate. Dependent on pressure.")
	private static double grateEffectMultiplier = 5;
	@Config(comment = "The speed in which the grate drains blocks. Dependent on grate block influence.")
	private static double grateDrainSpeedMultiplier = 0.01;

	@SideOnly(Side.CLIENT)
	private static Icon iconFront, iconSide;

	private GratePathfinder gratePath;

	private boolean fillOver = true;

	public TileGrate()
	{
		super(Material.rock);
		isOpaqueCube = false;
		normalRender = true;
		rotationMask = Byte.parseByte("111111", 2);
		node = new FluidPressureNode(this);
		node.maxFlowRate = getPressureTank().getCapacity();
	}

	@Override
	public Icon getIcon(IBlockAccess world, int side)
	{
		return side == getDirection().ordinal() ? iconFront : iconSide;
	}

	@Override
	public Icon getIcon(int side, int metadata)
	{
		return side == 1 ? iconFront : iconSide;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister)
	{
		iconFront = iconRegister.registerIcon(Reference.PREFIX + "grate_front");
		iconSide = iconRegister.registerIcon(Reference.PREFIX + "grate");
	}

	@Override
	protected boolean configure(EntityPlayer player, int side, Vector3 hit)
	{
		if (!player.isSneaking())
		{
			if (!world().isRemote)
			{
				fillOver = !fillOver;
				player.addChatMessage("Grate now set to " + (fillOver ? "" : "not ") + "fill higher than itself.");
				gratePath = null;
			}
			return true;
		}

		return super.configure(player, side, hit);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		fillOver = nbt.getBoolean("fillOver");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setBoolean("fillOver", fillOver);
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[] { getPressureTank().getInfo() };
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
	public void updateEntity()
	{
		super.updateEntity();

		if (!world().isRemote)
		{
			if (ticks % 10 == 0)
			{
				int pressure = node.getPressure(getDirection());
				int blockEffect = (int) Math.abs(pressure * grateEffectMultiplier);
				getPressureTank().setCapacity((int) Math.max(blockEffect * FluidContainerRegistry.BUCKET_VOLUME * grateDrainSpeedMultiplier, FluidContainerRegistry.BUCKET_VOLUME));

				if (pressure > 0)
				{
					// Fill
					if (getPressureTank().getFluidAmount() >= FluidContainerRegistry.BUCKET_VOLUME)
					{
						if (gratePath == null)
						{
							gratePath = new GratePathfinder(true);
							gratePath.startFill(new Vector3(this), getPressureTank().getFluid().getFluid().getID());
						}

						int filledInWorld = gratePath.tryFill(getPressureTank().getFluidAmount(), blockEffect);
						getPressureTank().drain(filledInWorld, true);
					}
				}
				else if (pressure < 0)
				{
					// Drain
					int maxDrain = getPressureTank().getCapacity() - getPressureTank().getFluidAmount();

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

						if (gratePath != null && gratePath.tryPopulateDrainMap(blockEffect))
						{
							getPressureTank().fill(gratePath.tryDrain(maxDrain, true), true);
						}
					}
				}
			}
		}
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		return getPressureTank().fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if (resource != null)
		{
			return drain(from, resource.amount, doDrain);
		}
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return getPressureTank().drain(maxDrain, doDrain);
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

	public static class ComparableVector implements Comparable
	{
		public Vector3 position;
		public int iterations;

		public ComparableVector(Vector3 position, int iterations)
		{
			this.position = position;
			this.iterations = iterations;
		}

		@Override
		public int compareTo(Object obj)
		{
			ComparableVector wr = (ComparableVector) obj;
			if (this.position.y == wr.position.y)
			{
				return this.iterations - wr.iterations;
			}
			return this.position.intY() - wr.position.intY();
		}
	}

	public class GratePathfinder
	{
		/**
		 * The type of fluid we're dealing with. When draining, this will be the type of fluid being
		 * drained.
		 */
		public Fluid fluidType;
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
			{
				return true;
			}
			do
			{
				check = this.navigationMap.get(check);

				if (check == null)
				{
					return false;
				}

				if (check.equals(this.start))
				{
					return true;
				}
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
			int filled = 0;

			if (amount >= FluidContainerRegistry.BUCKET_VOLUME)
			{
				for (int i = 0; i < tries; i++)
				{
					ComparableVector next = workingNodes.poll();

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

					int didFill = FluidUtility.fillBlock(TileGrate.this.worldObj, next.position, new FluidStack(fluidType, amount), true);
					filled += didFill;

					if (FluidUtility.getFluidAmountFromBlock(TileGrate.this.worldObj, next.position) > 0 || didFill > 0)
					{
						addNextFill(next);
					}

					if (filled >= amount)
					{
						return filled;
					}
				}
			}

			return filled;
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

				if (!this.navigationMap.containsKey(newPosition) && !(!fillOver && newPosition.intY() > y()))
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
		 * Creates a map of all the fluids to be drained.
		 *
		 * @param tries
		 * @return True if there is drainable fluid.
		 */
		public boolean tryPopulateDrainMap(int tries)
		{
			for (int i = 0; i < tries; i++)
			{
				ComparableVector check = workingNodes.poll();

				if (check == null)
				{
					return true;
				}

				Fluid checkFluid = FluidUtility.getFluidFromBlock(TileGrate.this.worldObj, check.position);

				if (checkFluid != null && fluidType.getID() == checkFluid.getID())
				{
					addNextDrain(check);

					int checkAmount = FluidUtility.getFluidAmountFromBlock(TileGrate.this.worldObj, check.position);

					if (checkAmount > 0)
					{
						drainNodes.add(check);
					}
				}
			}

			return drainNodes.size() > 0;
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
		public FluidStack tryDrain(int targetAmount, boolean doDrain)
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

					if (drainedAmount + checkAmount > targetAmount)
					{
						break;
					}

					if (checkAmount == 0)
					{
						this.drainNodes.poll();
					}
					else
					{
						FluidStack fluidStack = FluidUtility.drainBlock(TileGrate.this.worldObj, fluidCoord.position, doDrain);

						this.drainNodes.poll();

						if (fluidStack != null)
						{
							drainedAmount += fluidStack.amount;

							if (drainedAmount >= targetAmount)
							{
								break;
							}
						}
					}
				}
			}

			TileGrate.this.resetPath();

			if (drainedAmount > 0)
			{
				return new FluidStack(fluidType, drainedAmount);
			}

			return null;
		}

	}
}

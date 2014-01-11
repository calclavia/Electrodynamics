package resonantinduction.mechanical.fluid.pump;

import java.util.Iterator;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.core.prefab.tile.TileEntityEnergyMachine;
import resonantinduction.core.tilenetwork.ITileConnector;
import resonantinduction.old.api.fluid.IDrain;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.FluidHelper;

import com.builtbroken.common.Pair;

import dark.lib.interfaces.IReadOut;

public class TileEntityStarterPump extends TileEntityEnergyMachine implements IReadOut, ITileConnector
{
	private int currentWorldEdits, MAX_WORLD_EDITS_PER_PROCESS;

	public long ENERGY_PER_DRAIN = 50;

	private LiquidPathFinder pathLiquid;
	private Vector3 lastDrainOrigin;

	public int rotation = 0;

	public TileEntityStarterPump()
	{
		this(10, 50, 5);
	}

	/**
	 * @param wattTick - cost in watts per tick to run the tile
	 * @param wattDrain - cost in watts to drain or fill one block
	 * @param maxEdits - max world edits per update (1/2 second)
	 */
	public TileEntityStarterPump(long wattTick, long wattDrain, int maxEdits)
	{
		// Power calculation for max power (worldEdits * watts per edit) + (watts per tick * one
		// second)
		super(wattTick, (maxEdits * wattDrain) + (wattTick * 20));
		this.MAX_WORLD_EDITS_PER_PROCESS = maxEdits;
		this.ENERGY_PER_DRAIN = wattDrain;
	}

	/** Liquid path finder used by this tile. Retrieve the path finder if using IDrain block */
	public LiquidPathFinder getLiquidFinder()
	{
		if (pathLiquid == null)
		{
			pathLiquid = new LiquidPathFinder(this.worldObj, 100, 20);
		}
		return pathLiquid;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (this.ticks % 10 == 0)
		{
			this.currentWorldEdits = 0;

			if (this.isFunctioning())
			{
				this.rotation = Math.max(Math.min(this.rotation + 1, 7), 0);

				if (!this.worldObj.isRemote)
				{
					Pair<World, Vector3> pair = this.getDrainOrigin();
					if (pair != null && pair.left() != null && pair.right() != null)
					{
						this.drainAroundArea(pair.left(), pair.right(), 3);
					}
				}
			}

		}

	}

	/** Gets the origin the path finder starts on */
	public Pair<World, Vector3> getDrainOrigin()
	{
		// TODO change this to lower by the amount of air between the pump and bottom
		return new Pair<World, Vector3>(this.worldObj, new Vector3(this).modifyPositionFromSide(ForgeDirection.DOWN));
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
		TileEntity entity = null;

		Set<Vector3> drainList = null;
		if (drain instanceof IDrain)
		{
			if (!((IDrain) drain).canDrain(((IDrain) drain).getDirection()))
			{
				return;
			}
			origin = vec.modifyPositionFromSide(((IDrain) drain).getDirection());
			entity = origin.getTileEntity(world);
			if (entity instanceof IFluidHandler)
			{
				FluidStack draStack = ((IFluidHandler) entity).drain(ForgeDirection.UP, MAX_WORLD_EDITS_PER_PROCESS * FluidContainerRegistry.BUCKET_VOLUME, false);

				if (draStack != null && FluidHelper.fillTanksAllSides(worldObj, new Vector3(this), draStack, false, ForgeDirection.DOWN) > 0)
				{
					((IFluidHandler) entity).drain(ForgeDirection.UP, FluidHelper.fillTanksAllSides(worldObj, new Vector3(this), draStack, true, ForgeDirection.DOWN), true);

				}
				return;// TODO check why return is here
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

		if (entity == null && drainList != null && drainList.size() > 0)
		{
			// System.out.println("StartPump>>DrainArea>>Targets>" +
			// this.getLiquidFinder().results.size());

			Iterator<Vector3> fluidList = drainList.iterator();

			while (fluidList.hasNext() && this.consumePower(ENERGY_PER_DRAIN, false))
			{
				if (this.currentWorldEdits >= MAX_WORLD_EDITS_PER_PROCESS)
				{
					break;
				}

				Vector3 drainLocation = fluidList.next();
				FluidStack drainStack = FluidHelper.drainBlock(world, drainLocation, false, 3);
				// System.out.println("StartPump>>DrainArea>>Draining>>NextFluidBlock>" +
				// (drainStack == null ? "Null" : drainStack.amount + "mb of " +
				// drainStack.getFluid().getName()));

				// int fillV = FluidHelper.fillTanksAllSides(worldObj, new Vector3(this),
				// drainStack, false, ForgeDirection.DOWN);
				// System.out.println("StartPump>>DrainArea>>Draining>>NextFluidBlock>Filled>" +
				// fillV + "mb");

				if (drainStack != null && this.fill(drainStack, false) >= drainStack.amount && this.consumePower(ENERGY_PER_DRAIN, true))
				{
					// System.out.println("StartPump>>DrainArea>>Draining>>Fluid>" +
					// drainLocation.toString());
					/* REMOVE BLOCK */
					FluidHelper.drainBlock(this.worldObj, drainLocation, true, update);
					this.fill(drainStack, true);
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

	public int fill(FluidStack stack, boolean doFill)
	{
		return FluidHelper.fillTanksAllSides(worldObj, new Vector3(this), stack, doFill, ForgeDirection.DOWN);
	}

	@Override
	public boolean canFunction()
	{
		return super.canFunction() && worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
	}

	@Override
	public String getMeterReading(EntityPlayer user, ForgeDirection side, EnumTools tool)
	{
		if (tool == EnumTools.PIPE_GUAGE)
		{
			return "Source Blocks: " + this.getLiquidFinder().results.size();
		}
		return null;
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return direction != ForgeDirection.DOWN;
	}

	@Override
	public boolean canTileConnect(Connection type, ForgeDirection direction)
	{
		return direction != ForgeDirection.DOWN;
	}

}

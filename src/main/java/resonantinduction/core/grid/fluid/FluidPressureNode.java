package resonantinduction.core.grid.fluid;

import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.grid.Node;
import calclavia.lib.grid.TickingGrid;
import codechicken.multipart.TMultiPart;

public class FluidPressureNode extends Node<IPressureNodeProvider, TickingGrid, Object>
{
	protected byte connectionMap = Byte.parseByte("111111", 2);
	private int pressure = 0;
	public int maxFlowRate = 20;
	public int maxPressure = 100;

	public FluidPressureNode(IPressureNodeProvider parent)
	{
		super(parent);
	}

	public FluidPressureNode setConnection(byte connectionMap)
	{
		this.connectionMap = connectionMap;
		return this;
	}

	@Override
	public void update(float deltaTime)
	{
		if (!world().isRemote)
		{
			updatePressure();
			distribute();
		}
	}

	protected void updatePressure()
	{
		int totalPressure = 0;
		int findCount = 0;
		int minPressure = 0;
		int maxPressure = 0;

		Iterator<Entry<Object, ForgeDirection>> it = getConnections().entrySet().iterator();

		while (it.hasNext())
		{
			Entry<Object, ForgeDirection> entry = it.next();
			Object obj = entry.getKey();

			if (obj instanceof FluidPressureNode)
			{
				int pressure = ((FluidPressureNode) obj).getPressure(entry.getValue().getOpposite());

				minPressure = Math.min(pressure, minPressure);
				maxPressure = Math.max(pressure, maxPressure);
				totalPressure += pressure;
				findCount++;
			}
		}

		if (findCount == 0)
		{
			setPressure(0);
		}
		else
		{
			/**
			 * Create pressure loss.
			 */
			if (minPressure < 0)
				minPressure += 1;
			if (maxPressure > 0)
				maxPressure -= 1;

			setPressure(Math.max(minPressure, Math.min(maxPressure, totalPressure / findCount + Integer.signum(totalPressure))));
		}
	}

	public void distribute()
	{

		Iterator<Entry<Object, ForgeDirection>> it = getConnections().entrySet().iterator();

		while (it.hasNext())
		{
			Entry<?, ForgeDirection> entry = it.next();
			Object obj = entry.getKey();
			ForgeDirection dir = entry.getValue();

			if (obj instanceof FluidPressureNode)
			{
				/**
				 * Move fluid from higher pressure to lower. In this case, move from tankA to
				 * tankB.
				 */
				FluidPressureNode otherNode = (FluidPressureNode) obj;

				// A is the pressure in the current node while B is the pressure in the node we
				// are iterating through.
				int pressureA = getPressure(dir);
				int pressureB = otherNode.getPressure(dir.getOpposite());

				if (pressureA >= pressureB)
				{
					FluidTank tankA = parent.getPressureTank();

					if (tankA != null)
					{
						FluidStack fluidA = tankA.getFluid();

						if (fluidA != null)
						{
							int amountA = fluidA.amount;

							if (amountA > 0)
							{
								FluidTank tankB = otherNode.parent.getPressureTank();

								if (tankB != null)
								{
									int amountB = tankB.getFluidAmount();

									int quantity = Math.max(pressureA > pressureB ? (pressureA - pressureB) * getMaxFlowRate() : 0, Math.min((amountA - amountB) / 2, getMaxFlowRate()));
									quantity = Math.min(Math.min(quantity, tankB.getCapacity() - amountB), amountA);

									if (quantity > 0)
									{
										FluidStack drainStack = parent.drain(dir.getOpposite(), quantity, false);

										if (drainStack != null && drainStack.amount > 0)
											parent.drain(dir.getOpposite(), otherNode.parent.fill(dir, drainStack, true), true);
									}
								}
							}
						}
					}
				}
			}
			else if (obj instanceof IFluidHandler)
			{
				IFluidHandler fluidHandler = (IFluidHandler) obj;
				int pressure = getPressure(dir);
				int tankPressure = fluidHandler instanceof IPressureNodeProvider ? ((IPressureNodeProvider) fluidHandler).getNode(FluidPressureNode.class, dir.getOpposite()).getPressure(dir.getOpposite()) : 0;
				FluidTank sourceTank = parent.getPressureTank();

				int transferAmount = (Math.max(pressure, tankPressure) - Math.min(pressure, tankPressure)) * getMaxFlowRate();

				if (pressure > tankPressure)
				{
					if (sourceTank.getFluidAmount() > 0 && transferAmount > 0)
					{
						FluidStack drainStack = parent.drain(dir.getOpposite(), transferAmount, false);
						parent.drain(dir.getOpposite(), fluidHandler.fill(dir.getOpposite(), drainStack, true), true);
					}
				}
				else if (pressure < tankPressure)
				{
					if (transferAmount > 0)
					{
						FluidStack drainStack = fluidHandler.drain(dir.getOpposite(), transferAmount, false);

						if (drainStack != null)
						{
							fluidHandler.drain(dir.getOpposite(), parent.fill(dir.getOpposite(), drainStack, true), true);
						}
					}
				}
			}
		}
	}

	public int getMaxFlowRate()
	{
		return maxFlowRate;
	}

	public void setPressure(int newPressure)
	{
		if (newPressure > 0)
			pressure = Math.min(maxPressure, newPressure);
		else
			pressure = Math.max(-maxPressure, newPressure);
	}

	public int getPressure(ForgeDirection dir)
	{
		return pressure;
	}

	/**
	 * Recache the connections. This is the default connection implementation.
	 */
	@Override
	public void recache()
	{
		synchronized (connections)
		{
			connections.clear();

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tile = position().translate(dir).getTileEntity(world());

				if (tile instanceof IPressureNodeProvider)
				{
					FluidPressureNode check = ((IPressureNodeProvider) tile).getNode(FluidPressureNode.class, dir.getOpposite());

					if (check != null && canConnect(dir, check) && check.canConnect(dir.getOpposite(), this))
					{
						connections.put(check, dir);
					}
				}
			}
		}
	}

	public World world()
	{
		return parent instanceof TMultiPart ? ((TMultiPart) parent).world() : parent instanceof TileEntity ? ((TileEntity) parent).getWorldObj() : null;
	}

	public Vector3 position()
	{
		return parent instanceof TMultiPart ? new Vector3(((TMultiPart) parent).x(), ((TMultiPart) parent).y(), ((TMultiPart) parent).z()) : parent instanceof TileEntity ? new Vector3((TileEntity) parent) : null;
	}

	@Override
	public boolean canConnect(ForgeDirection from, Object source)
	{
		return (source instanceof FluidPressureNode) && (connectionMap & (1 << from.ordinal())) != 0;
	}

	@Override
	public TickingGrid newGrid()
	{
		return new TickingGrid<FluidPressureNode>(this, FluidPressureNode.class);
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);
		pressure = nbt.getInteger("pressure");
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);
		nbt.setInteger("pressure", pressure);
	}

}

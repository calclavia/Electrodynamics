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
import resonantinduction.core.grid.Node;
import resonantinduction.core.grid.TickingGrid;
import universalelectricity.api.vector.Vector3;
import codechicken.multipart.TMultiPart;

public class PressureNode extends Node<IPressureNodeProvider, TickingGrid, Object>
{
	protected byte connectionMap = Byte.parseByte("111111", 2);
	public int pressure = 0;

	public PressureNode(IPressureNodeProvider parent)
	{
		super(parent);
	}

	public PressureNode setConnection(byte connectionMap)
	{
		this.connectionMap = connectionMap;
		return this;
	}

	@Override
	public void update(float deltaTime)
	{
		updatePressure();
		distribute();
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

			if (obj instanceof PressureNode)
			{
				int pressure = ((PressureNode) obj).getPressure(entry.getValue().getOpposite());

				minPressure = Math.min(pressure, minPressure);
				maxPressure = Math.max(pressure, maxPressure);
				totalPressure += pressure;
				findCount++;
			}
		}

		if (findCount == 0)
		{
			pressure = 0;
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

			pressure = Math.max(minPressure, Math.min(maxPressure, totalPressure / findCount + Integer.signum(totalPressure)));
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

			if (obj instanceof PressureNode)
			{
				PressureNode otherPipe = (PressureNode) obj;

				/**
				 * Move fluid from higher pressure to lower. In this case, move from tankA to tankB.
				 */
				int pressureA = getPressure(dir);
				int pressureB = otherPipe.getPressure(dir.getOpposite());

				if (pressureA >= pressureB)
				{
					FluidTank tankA = parent.getPressureTank();
					FluidStack fluidA = tankA.getFluid();

					if (tankA != null && fluidA != null)
					{
						int amountA = fluidA.amount;

						if (amountA > 0)
						{
							FluidTank tankB = otherPipe.parent.getPressureTank();

							if (tankB != null)
							{
								int amountB = tankB.getFluidAmount();

								int quantity = Math.max(pressureA > pressureB ? (pressureA - pressureB) * getMaxFlowRate() : 0, Math.min((amountA - amountB) / 2, getMaxFlowRate()));
								quantity = Math.min(Math.min(quantity, tankB.getCapacity() - amountB), amountA);

								if (quantity > 0)
								{
									FluidStack drainStack = parent.drain(dir.getOpposite(), quantity, false);

									if (drainStack != null && drainStack.amount > 0)
										parent.drain(dir.getOpposite(), otherPipe.parent.fill(dir, drainStack, true), true);
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
				int tankPressure = fluidHandler instanceof IPressureNodeProvider ? ((IPressureNodeProvider) fluidHandler).getNode(PressureNode.class, dir.getOpposite()).getPressure(dir.getOpposite()) : 0;
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

	protected int getMaxFlowRate()
	{
		return 10;
	}

	public int getPressure(ForgeDirection dir)
	{
		return pressure;
	}

	/**
	 * Recache the connections. This is the default connection implementation.
	 */
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
					PressureNode check = ((IPressureNodeProvider) tile).getNode(PressureNode.class, dir.getOpposite());

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

	public boolean canConnect(ForgeDirection from, Object source)
	{
		return (source instanceof PressureNode) && (connectionMap & (1 << from.ordinal())) != 0;
	}

	@Override
	public TickingGrid newGrid()
	{
		return new TickingGrid<PressureNode>(this, PressureNode.class);
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

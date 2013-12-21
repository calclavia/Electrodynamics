package resonantinduction.wire;

import resonantinduction.ResonantInduction;
import calclavia.lib.prefab.block.EnergyStorage;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.IConnector;
import universalelectricity.api.INetworkProvider;
import universalelectricity.api.UniversalClass;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyInterface;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorHelper;
import universalelectricity.core.grid.EnergyNetworkLoader;
import universalelectricity.core.grid.IEnergyNetwork;
import codechicken.multipart.TileMultipart;

@UniversalClass
public abstract class PartConductor extends PartAdvanced implements IConductor
{
	private IEnergyNetwork network;
	private EnergyStorage buffer = new EnergyStorage(ResonantInduction.FURNACE_WATTAGE * 5);

	public TileEntity[] adjacentConnections = null;
	public byte currentWireConnections = 0x00;
	public byte currentAcceptorConnections = 0x00;

	@Override
	public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
	{
		return buffer.receiveEnergy(receive, doReceive);
	}

	@Override
	public long onExtractEnergy(ForgeDirection from, long request, boolean doExtract)
	{
		return buffer.extractEnergy(request, doExtract);
	}

	@Override
	public void distribute()
	{
		Object[] receivers = this.getAdjacentConnections();
		int energyUsed = 0;

		for (int i = 0; i < receivers.length; i++)
		{
			ForgeDirection direction = ForgeDirection.getOrientation(i);
			Object receiver = receivers[i];

			if (receiver instanceof IEnergyInterface && !(receiver instanceof IConductor))
			{
				energyUsed += ((IEnergyInterface) receiver).onReceiveEnergy(direction.getOpposite(), this.getNetwork().getDistribution(this), true);
			}
		}

		this.onExtractEnergy(null, energyUsed, true);
	}

	@Override
	public int getEnergyLoss()
	{
		return 0;
	}

	@Override
	public int getEnergyCapacitance()
	{
		return 0;
	}

	public byte getAllCurrentConnections()
	{
		return (byte) (currentWireConnections | currentAcceptorConnections);
	}

	public static boolean connectionMapContainsSide(byte connections, ForgeDirection side)
	{
		byte tester = (byte) (1 << side.ordinal());
		return ((connections & tester) > 0);
	}

	@Override
	public void onMoved()
	{
		this.refresh();
	}

	@Override
	public void bind(TileMultipart t)
	{
		if (tile() != null && network != null)
		{
			getNetwork().getConnectors().remove(tile());
			super.bind(t);
			getNetwork().getConnectors().add((IConductor) tile());
		}
		else
		{
			super.bind(t);
		}
	}

	@Override
	public void preRemove()
	{
		if (!world().isRemote && tile() instanceof IConductor)
		{
			getNetwork().split((IConductor) tile());
		}

		super.preRemove();
	}

	@Override
	public boolean doesTick()
	{
		return false;
	}

	@Override
	public IEnergyNetwork getNetwork()
	{
		if (network == null && tile() instanceof IConductor)
		{
			setNetwork(EnergyNetworkLoader.getNewNetwork((IConductor) tile()));
		}

		return network;
	}

	public boolean canConnectBothSides(TileEntity tile, ForgeDirection side)
	{
		boolean notPrevented = !isConnectionPrevented(tile, side);

		if (tile instanceof IConnector)
		{
			notPrevented &= ((IConnector) tile).canConnect(side.getOpposite());
		}

		return notPrevented;
	}

	@Override
	public void setNetwork(IEnergyNetwork net)
	{
		network = net;
	}

	/**
	 * Override if there are ways of preventing a connection
	 * 
	 * @param tile The TileEntity on the given side
	 * @param side The side we're checking
	 * @return Whether we're preventing connections on given side or to given tileEntity
	 */
	public boolean isConnectionPrevented(TileEntity tile, ForgeDirection side)
	{
		return false;
	}

	public byte getPossibleWireConnections()
	{
		byte connections = 0x00;

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tileEntity = VectorHelper.getTileEntityFromSide(world(), new Vector3(tile()), side);

			if (tileEntity instanceof INetworkProvider && canConnectBothSides(tileEntity, side))
			{
				connections |= 1 << side.ordinal();
			}
		}

		return connections;
	}

	public byte getPossibleAcceptorConnections()
	{
		byte connections = 0x00;

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tileEntity = VectorHelper.getTileEntityFromSide(world(), new Vector3(tile()), side);

			if (isValidAcceptor(tileEntity) && canConnectBothSides(tileEntity, side))
			{
				connections |= 1 << side.ordinal();
			}
		}

		return connections;
	}

	/**
	 * Override if there are different kinds of acceptor possible
	 */
	public boolean isValidAcceptor(TileEntity tile)
	{
		return tile instanceof IConnector;
	}

	public void refresh()
	{
		if (!world().isRemote)
		{
			adjacentConnections = null;

			byte possibleWireConnections = getPossibleWireConnections();
			byte possibleAcceptorConnections = getPossibleAcceptorConnections();

			if (possibleWireConnections != currentWireConnections)
			{
				byte or = (byte) (possibleWireConnections | currentWireConnections);

				if (or != possibleWireConnections) // Connections have been removed
				{
					getNetwork().split((IConductor) tile());
					setNetwork(null);
				}

				for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
				{
					if (connectionMapContainsSide(possibleWireConnections, side))
					{
						TileEntity tileEntity = VectorHelper.getConnectorFromSide(world(), new Vector3(tile()), side);

						if (tileEntity instanceof IConductor)
						{
							getNetwork().merge(((IConductor) tileEntity).getNetwork());
						}
					}
				}

				this.currentWireConnections = possibleWireConnections;
			}

			this.currentAcceptorConnections = possibleAcceptorConnections;

			this.sendDescUpdate();
		}

		tile().markRender();
	}

	/**
	 * Should include connections that are in the current connection maps even if those connections
	 * aren't allowed any more. This is so that networks split correctly.
	 */
	@Override
	public TileEntity[] getAdjacentConnections()
	{
		if (this.adjacentConnections == null)
		{
			this.adjacentConnections = new TileEntity[6];

			for (byte i = 0; i < 6; i++)
			{
				ForgeDirection side = ForgeDirection.getOrientation(i);
				TileEntity tileEntity = VectorHelper.getTileEntityFromSide(world(), new Vector3(tile()), side);

				if (isCurrentlyConnected(side))
				{
					this.adjacentConnections[i] = tileEntity;
				}
			}
		}
		return this.adjacentConnections;
	}

	public boolean isCurrentlyConnected(ForgeDirection side)
	{
		return connectionMapContainsSide(getAllCurrentConnections(), side);
	}

	/**
	 * Shouldn't need to be overridden. Override connectionPrevented instead
	 */
	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		Vector3 connectPos = new Vector3(tile()).modifyPositionFromSide(direction);
		TileEntity connectTile = connectPos.getTileEntity(world());
		return !isConnectionPrevented(connectTile, direction);
	}

	@Override
	public void onAdded()
	{
		super.onAdded();
		refresh();
	}

	@Override
	public void onChunkLoad()
	{
		super.onChunkLoad();
		refresh();
	}

	@Override
	public void onNeighborChanged()
	{
		super.onNeighborChanged();
		refresh();
	}

}

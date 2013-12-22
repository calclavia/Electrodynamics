package resonantinduction.wire.part;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.base.PartAdvanced;
import resonantinduction.wire.IAdvancedConductor;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.energy.EnergyNetworkLoader;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyNetwork;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorHelper;
import codechicken.multipart.TileMultipart;

//@UniversalClass
public abstract class PartConductor extends PartAdvanced implements IAdvancedConductor
{
	private IEnergyNetwork network;

	public byte currentWireConnections = 0x00;
	public byte currentAcceptorConnections = 0x00;

	/**
	 * Universal Electricity conductor functions.
	 */
	@Override
	public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
	{
		return this.getNetwork().produce(receive);
	}

	@Override
	public long onExtractEnergy(ForgeDirection from, long request, boolean doExtract)
	{
		return 0;
	}

	@Override
	public IEnergyNetwork getNetwork()
	{
		if (this.network == null && tile() instanceof IConductor)
		{
			setNetwork(EnergyNetworkLoader.getNewNetwork((IConductor) tile()));
		}

		return network;
	}

	@Override
	public void setNetwork(IEnergyNetwork network)
	{
		this.network = network;
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

	public boolean canConnectBothSides(TileEntity tile, ForgeDirection side)
	{
		boolean notPrevented = !isConnectionPrevented(tile, side);

		if (tile instanceof IConductor)
		{
			notPrevented &= ((IConductor) tile).canConnect(side.getOpposite());
		}

		return notPrevented;
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

			if (tileEntity instanceof IConductor && canConnectBothSides(tileEntity, side))
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

			if (CompatibilityModule.canConnect(tileEntity, side.getOpposite()) && canConnectBothSides(tileEntity, side))
			{
				connections |= 1 << side.ordinal();
			}
		}

		return connections;
	}

	public void refresh()
	{
		if (!world().isRemote)
		{
			byte possibleWireConnections = getPossibleWireConnections();
			byte possibleAcceptorConnections = getPossibleAcceptorConnections();

			if (possibleWireConnections != this.currentWireConnections)
			{
				byte or = (byte) (possibleWireConnections | this.currentWireConnections);

				// Connections have been removed
				if (or != possibleWireConnections)
				{
					this.getNetwork().split((IConductor) tile());
					this.setNetwork(null);
				}

				for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
				{
					if (connectionMapContainsSide(possibleWireConnections, side))
					{
						TileEntity tileEntity = VectorHelper.getConnectorFromSide(world(), new Vector3(tile()), side);

						if (tileEntity instanceof IConductor)
						{
							this.getNetwork().merge(((IConductor) tileEntity).getNetwork());
						}
					}
				}

				this.currentWireConnections = possibleWireConnections;
			}

			this.currentAcceptorConnections = possibleAcceptorConnections;
			this.getNetwork().reconstruct();
			// this.sendDescUpdate();
		}

		tile().markRender();
	}

	/**
	 * Should include connections that are in the current connection maps even if those connections
	 * aren't allowed any more. This is so that networks split correctly.
	 */
	@Override
	public TileEntity[] getConnections()
	{
		TileEntity[] cachedConnections = new TileEntity[6];

		for (byte i = 0; i < 6; i++)
		{
			ForgeDirection side = ForgeDirection.getOrientation(i);
			TileEntity tileEntity = VectorHelper.getTileEntityFromSide(world(), new Vector3(tile()), side);

			if (isCurrentlyConnected(side))
			{
				cachedConnections[i] = tileEntity;
			}
		}

		return cachedConnections;
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
	public void onMoved()
	{
		this.refresh();
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

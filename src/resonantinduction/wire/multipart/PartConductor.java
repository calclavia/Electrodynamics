package resonantinduction.wire.multipart;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.block.IConductor;
import universalelectricity.core.block.IConnector;
import universalelectricity.core.block.INetworkProvider;
import universalelectricity.core.electricity.NetworkLoader;
import universalelectricity.core.grid.IElectricityNetwork;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;

public abstract class PartConductor extends PartAdvanced implements IConductor
{
	private IElectricityNetwork network;

	public TileEntity[] adjacentConnections = null;

	@Override
	public void preRemove()
	{
		if (!this.world().isRemote && this.getTile() instanceof IConductor)
		{
			this.getNetwork().split((IConductor)this.getTile());
		}

		super.preRemove();
	}

	@Override
	public boolean doesTick()
	{
		return false;
	}

	@Override
	public IElectricityNetwork getNetwork()
	{
		if (this.network == null && this.getTile() instanceof IConductor)
		{
			this.setNetwork(NetworkLoader.getNewNetwork((IConductor)this.getTile()));
		}

		return this.network;
	}

	@Override
	public void setNetwork(IElectricityNetwork network)
	{
		this.network = network;
	}

	@Override
	public void refresh()
	{
		if (!this.world().isRemote)
		{
			this.adjacentConnections = null;

			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tileEntity = VectorHelper.getConnectorFromSide(this.world(), new Vector3(tile()), side);

				if (tileEntity != null)
				{
					if (tileEntity.getClass() == tile().getClass() && tileEntity instanceof INetworkProvider)
					{
						this.getNetwork().merge(((INetworkProvider) tileEntity).getNetwork());
					}
				}
			}

			this.getNetwork().refresh();
		}
	}

	@Override
	public TileEntity[] getAdjacentConnections()
	{
		/**
		 * Cache the adjacentConnections.
		 */
		if (this.adjacentConnections == null)
		{
			this.adjacentConnections = new TileEntity[6];

			for (byte i = 0; i < 6; i++)
			{
				ForgeDirection side = ForgeDirection.getOrientation(i);
				TileEntity tileEntity = VectorHelper.getConnectorFromSide(this.world(), new Vector3(tile()), side);

				if (tileEntity instanceof IConnector)
				{
					if (((IConnector) tileEntity).canConnect(side.getOpposite()))
					{
						this.adjacentConnections[i] = tileEntity;
					}
				}
			}
		}

		return this.adjacentConnections;
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return true;
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

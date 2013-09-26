package resonantinduction.wire.multipart;

import codechicken.multipart.TileMultipart;
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
	public byte currentConnections = 0x00;

	
	@Override
	public void bind(TileMultipart t)
	{
		if (tile() != null && network != null)
		{
			this.getNetwork().getConductors().remove(tile());
			super.bind(t);
			this.getNetwork().getConductors().add((IConductor) tile());
		}
		else
		{
			super.bind(t);
		}
	}

	@Override
	public void preRemove()
	{
		if (!this.world().isRemote && this.tile() instanceof IConductor)
		{
			this.getNetwork().split((IConductor)this.tile());
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
		if (this.network == null && this.tile() instanceof IConductor)
		{
			this.setNetwork(NetworkLoader.getNewNetwork((IConductor)this.tile()));
		}

		return this.network;
	}

	@Override
	public void setNetwork(IElectricityNetwork network)
	{
		this.network = network;
	}

	public boolean connectionPrevented(TileEntity tile, ForgeDirection side)
	{
		return false;
	}

	public byte getPossibleConnections()
	{
		byte connections = 0x00;
		
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tileEntity = VectorHelper.getTileEntityFromSide(this.world(), new Vector3(tile()), side);
			if (tileEntity instanceof INetworkProvider && !this.connectionPrevented(tileEntity, side))
				connections |= 1 << side.ordinal();
		}
		return connections;
	}

	@Override
	public void refresh()
	{
		if (!this.world().isRemote)
		{
			this.adjacentConnections = null;
			byte possibleConnections = getPossibleConnections();
			if (possibleConnections != currentConnections)
			{
				byte or = (byte) (possibleConnections | currentConnections);
				if (or != possibleConnections) //Connections have been removed
				{
					this.getNetwork().split((IConductor) tile());
					this.setNetwork(null);
				}
				
				for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
				{
					byte tester = (byte) (1 << side.ordinal());
					if ((possibleConnections & tester) > 0)
					{
						TileEntity tileEntity = VectorHelper.getConnectorFromSide(this.world(), new Vector3(tile()), side);

						if (tileEntity instanceof INetworkProvider)
						{
							this.getNetwork().merge(((INetworkProvider) tileEntity).getNetwork());
						}
					}
				}

				currentConnections = possibleConnections;
				
			}
			this.sendDescUpdate();
			this.getNetwork().refresh();		
						
		}
		tile().markRender();
	}

	@Override
	public TileEntity[] getAdjacentConnections()
	{
		if (this.adjacentConnections == null)
		{
			this.adjacentConnections = new TileEntity[6];
	
			for (byte i = 0; i < 6; i++)
			{
				ForgeDirection side = ForgeDirection.getOrientation(i);
				TileEntity tileEntity = VectorHelper.getTileEntityFromSide(this.world(), new Vector3(tile()), side);
	
				if (isCurrentlyConnected(tileEntity, side))
				{
					adjacentConnections[i] = tileEntity;
				}
			}
		}
		return this.adjacentConnections;
	}
	
	public boolean isCurrentlyConnected(TileEntity tileEntity, ForgeDirection side)
	{
		if ((this.currentConnections & 1 << side.ordinal()) > 0)
		{
			return true;
		}
		
		if (!this.canConnect(side))
		{
			return false;
		}
		
		if (tileEntity instanceof IConnector && ((IConnector)tileEntity).canConnect(side.getOpposite()))
		{
			return true;
		}
		
		return false;
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		Vector3 connectPos = new Vector3(tile()).modifyPositionFromSide(direction);
		TileEntity connectTile = connectPos.getTileEntity(this.world());
		return !connectionPrevented(connectTile, direction);
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

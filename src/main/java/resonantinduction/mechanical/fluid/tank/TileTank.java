package resonantinduction.mechanical.fluid.tank;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.api.fluid.INetworkFluidPart;
import resonantinduction.core.tilenetwork.INetworkPart;
import resonantinduction.core.tilenetwork.ITileNetwork;
import resonantinduction.mechanical.fluid.network.NetworkFluidContainers;
import resonantinduction.mechanical.fluid.prefab.TileFluidNetworkTile;

public class TileTank extends TileFluidNetworkTile
{
	public TileTank()
	{
		super(BlockTank.tankVolume);
	}

	@Override
	public NetworkFluidContainers getTileNetwork()
	{
		if (!(this.network instanceof NetworkFluidContainers))
		{
			this.setTileNetwork(new NetworkFluidContainers(this));
		}
		return (NetworkFluidContainers) this.network;
	}

	@Override
	public void setTileNetwork(ITileNetwork network)
	{
		if (network instanceof NetworkFluidContainers)
		{
			this.network = (NetworkFluidContainers) network;
		}
	}

	@Override
	public void validateConnectionSide(TileEntity tileEntity, ForgeDirection side)
	{
		if (!this.worldObj.isRemote)
		{
			if (tileEntity instanceof TileTank)
			{
				if (this.canTileConnect(Connection.NETWORK, side.getOpposite()))
				{
					this.getTileNetwork().mergeNetwork(((INetworkFluidPart) tileEntity).getTileNetwork(), (INetworkPart) tileEntity);
					this.setRenderSide(side, true);
					connectedBlocks.add(tileEntity);
				}
			}
		}
	}
}

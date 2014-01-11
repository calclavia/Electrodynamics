package resonantinduction.mechanical.fluid;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.tilenetwork.INetworkPart;
import resonantinduction.core.tilenetwork.ITileNetwork;
import resonantinduction.mechanical.fluid.network.NetworkFluidContainers;
import resonantinduction.mechanical.fluid.prefab.TileEntityFluidNetworkTile;
import resonantinduction.old.api.fluid.INetworkFluidPart;

public class TileEntityTank extends TileEntityFluidNetworkTile
{
	public TileEntityTank()
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
			if (tileEntity instanceof TileEntityTank)
			{
				if (this.canTileConnect(Connection.NETWORK, side.getOpposite()))
				{
					this.getTileNetwork().mergeNetwork(((INetworkFluidPart) tileEntity).getTileNetwork(), (INetworkPart) tileEntity);
					this.renderConnection[side.ordinal()] = true;
					connectedBlocks.add(tileEntity);
				}
			}
		}
	}
}

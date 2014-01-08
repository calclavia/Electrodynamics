package com.builtbroken.assemblyline.machine;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import com.builtbroken.assemblyline.api.fluid.INetworkFluidPart;
import com.builtbroken.assemblyline.fluid.network.NetworkFluidContainers;
import com.builtbroken.assemblyline.fluid.prefab.TileEntityFluidNetworkTile;
import com.builtbroken.minecraft.tilenetwork.INetworkPart;
import com.builtbroken.minecraft.tilenetwork.ITileNetwork;

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

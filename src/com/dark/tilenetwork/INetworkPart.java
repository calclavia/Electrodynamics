package com.dark.tilenetwork;

import java.util.List;

import net.minecraft.tileentity.TileEntity;

public interface INetworkPart extends ITileConnector
{
    /** Array of connections this tile has to other tiles */
    public List<TileEntity> getNetworkConnections();

    /** Update the connection this tile has to other tiles */
    public void refresh();

    /** Gets the networkPart's primary network */
    public ITileNetwork getTileNetwork();

    /** Sets the networkPart's primary network */
    public void setTileNetwork(ITileNetwork network);
}

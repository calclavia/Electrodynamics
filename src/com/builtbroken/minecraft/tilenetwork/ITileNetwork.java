package com.builtbroken.minecraft.tilenetwork;

import java.util.Set;

import net.minecraft.tileentity.TileEntity;

/** Applies to objects that act as a collection of tile entities.
 * 
 * @author DarkGuardsman */
public interface ITileNetwork
{
    /** Gets the name of the network */
    public String getName();

    /** Gets a list of all tiles that are part of this network */
    public Set<INetworkPart> getMembers();

    /** Called when something want the network to add the tile
     * 
     * @param entity - tile in question
     * @param member - add it as a member if true
     * @return true if added without issue */
    public boolean addTile(TileEntity ent, boolean member);

    /** Removes a tile from all parts of the network */
    public boolean removeTile(TileEntity ent);

    /** Called when this network is just created */
    public void onCreated();

    /** How many ticks should base between updates, return 0 or bellow for no ticks */
    public int getUpdateRate();

    /** Called every so many ticks so the network has a chance to update */
    public void updateTick();

    /** Called every so many mins when the networks needs to refresh and repair. Each part should
     * still handle there own refresh when edited, or updated. This is more for the network to do
     * house cleaning */
    public void refreshTick();

    /** Called when two networks try to merge together */
    public void mergeNetwork(ITileNetwork network, INetworkPart mergePoint);

    /** Called when a peace of the network is removed and might need to split in two */
    public void splitNetwork(INetworkPart splitPoint);

    /** Check by the network handle if this network is invalid or no longer functional */
    public boolean isInvalid();

    /** This is called when your network is considered invalid. You should cut all ties in the
     * network to its object so GC will delete it */
    public void invalidate();

    /** Called when the network needs to save */
    public void save();

    /** Called when the network needs to load */
    public void load();
}

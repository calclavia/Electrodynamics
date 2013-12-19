package com.dark.tilenetwork.prefab;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;

import com.builtbroken.common.Pair;
import com.dark.tilenetwork.IMotionPath;
import com.dark.tilenetwork.INetworkPart;


/** Class that acts like the redpower pipes system. Each item is marked with a destination. Intended
 * use it to improve the assembly line network
 * 
 * @author DarkGuardsman */
public class NetworkItemSupply extends NetworkTileEntities
{
    List<Pair<Entity, Vector3>> trackingList = new ArrayList<Pair<Entity, Vector3>>();
    List<Entity> ignoreList = new ArrayList<Entity>();
    /** Same as valid directions from forge direction enum but Unknown was added so that is gets
     * check and checked first */
    public static final ForgeDirection[] VALID_DIRECTIONS = { ForgeDirection.UNKNOWN, ForgeDirection.DOWN, ForgeDirection.UP, ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.EAST };

    public NetworkItemSupply(INetworkPart... parts)
    {
        super(parts);
    }

    @Override
    public void updateTick()
    {
        Iterator<Pair<Entity, Vector3>> it = trackingList.iterator();
        while (it.hasNext())
        {
            Pair<Entity, Vector3> entry = it.next();
            if (entry.left() == null || !this.isOnPath(entry.left()))
            {
                it.remove();
            }
            else
            {
                if (entry.right() == null)
                {
                    entry.setRight(new Vector3(entry.left()));
                }
                else
                {
                    entry.left().setPosition(entry.right().x, entry.right().y, entry.right().z);
                }
            }
        }

    }

    @Override
    public int getUpdateRate()
    {
        return 1;
    }

    /** Remove an entity from the tracking list */
    public void removeEntity(Entity entity)
    {
        this.trackingList.remove(entity);
    }

    /** Ignores an entity so that is can be controlled by something else for a while. Eg armbots, and
     * drones */
    public void ignoreEntity(Entity entity)
    {
        if (!this.ignoreList.contains(entity))
        {
            this.ignoreList.add(entity);
        }
    }

    /** Add and entity to the tracking list */
    public void addEntity(Entity entity)
    {
        if (!this.trackingList.contains(entity))
        {
            this.trackingList.add(new Pair<Entity, Vector3>(entity, new Vector3(entity)));
        }
    }

    public boolean isTrackingEntity(Entity entity)
    {
        return this.trackingList.contains(entity);
    }

    public boolean isOnPath(Entity entity)
    {
        if (entity != null)
        {
            Vector3 ent = new Vector3(entity);
            //Check all directions including the current position of the entity
            for (ForgeDirection direction : NetworkItemSupply.VALID_DIRECTIONS)
            {
                TileEntity a = ent.clone().modifyPositionFromSide(direction).getTileEntity(entity.worldObj);
                if (a instanceof IMotionPath && ((IMotionPath) a).canMoveEntity(entity) && this.networkMembers.contains(a))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isValidMember(INetworkPart part)
    {
        return super.isValidMember(part) && part instanceof IMotionPath;
    }

}

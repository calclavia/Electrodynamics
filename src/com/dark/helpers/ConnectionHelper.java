package com.dark.helpers;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;

public class ConnectionHelper
{
    /** Used to find all tileEntities sounding the location you will have to filter for selective
     * tileEntities
     * 
     * @param world - the world being searched threw
     * @param x
     * @param y
     * @param z
     * @return an array of up to 6 tileEntities */
    public static TileEntity[] getSurroundingTileEntities(TileEntity ent)
    {
        return getSurroundingTileEntities(ent.worldObj, ent.xCoord, ent.yCoord, ent.zCoord);
    }

    public static TileEntity[] getSurroundingTileEntities(World world, Vector3 vec)
    {
        return getSurroundingTileEntities(world, vec.intX(), vec.intY(), vec.intZ());
    }

    public static TileEntity[] getSurroundingTileEntities(World world, int x, int y, int z)
    {
        TileEntity[] list = new TileEntity[6];
        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
        {
            list[direction.ordinal()] = world.getBlockTileEntity(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
        }
        return list;
    }

    /** Used to find which of 4 Corners this block is in a group of blocks 0 = not a corner 1-4 = a
     * corner of some direction */
    public static int corner(TileEntity entity)
    {
        TileEntity[] en = getSurroundingTileEntities(entity.worldObj, entity.xCoord, entity.yCoord, entity.zCoord);
        TileEntity north = en[ForgeDirection.NORTH.ordinal()];
        TileEntity south = en[ForgeDirection.SOUTH.ordinal()];
        TileEntity east = en[ForgeDirection.EAST.ordinal()];
        TileEntity west = en[ForgeDirection.WEST.ordinal()];

        if (west != null && north != null && east == null && south == null)
        {
            return 3;
        }
        if (north != null && east != null && south == null && west == null)
        {
            return 4;
        }
        if (east != null && south != null && west == null && north == null)
        {
            return 1;
        }
        if (south != null && west != null && north == null && east == null)
        {
            return 2;
        }

        return 0;
    }
}

package resonantinduction.mechanical.blockeditor;

import calclavia.lib.content.module.prefab.TileInventory;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;

/**
 * @since 18/03/14
 * @author tgame14
 */
public class BlockTilePlacer extends TileInventory
{
    public BlockTilePlacer ()
    {
        super("BlockTilePlacer", Material.iron);
    }

    @Override
    public int getWeakRedstonePower (IBlockAccess access, int side)
    {
        int facing = access.getBlockMetadata(x(), y(), z());
        ForgeDirection dir = ForgeDirection.getOrientation(facing);
        if (access.isAirBlock(x() + dir.offsetX, y() + dir.offsetY, z() + dir.offsetZ))
        {
            getWorldObj().setBlock(x() + dir.offsetX, y() + dir.offsetY, z() + dir.offsetZ, getInventory().getContainedItems()[0].getItem().itemID);
        }

        return super.getWeakRedstonePower(access, side);
    }
}

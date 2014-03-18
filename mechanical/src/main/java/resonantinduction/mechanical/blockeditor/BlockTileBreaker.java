package resonantinduction.mechanical.blockeditor;

import calclavia.lib.content.module.prefab.TileInventory;
import calclavia.lib.prefab.tile.IRotatable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.ResonantInduction;

/**
 * @since 18/03/14
 * @author tgame14
 */
public class BlockTileBreaker extends TileInventory implements IRotatable
{

    public BlockTileBreaker ()
    {
        super("BlockTileBreaker", Material.iron);
    }

    @Override
    public int getWeakRedstonePower (IBlockAccess access, int side)
    {
        ResonantInduction.LOGGER.info("Calling Weak Red on tile ");
        int facing = access.getBlockMetadata(x(), y(), z());
        if (facing != side)
        {
            ForgeDirection dir = ForgeDirection.getOrientation(facing);
            Block block = Block.blocksList[access.getBlockId(x() + dir.offsetX, y() + dir.offsetY, z() + dir.offsetZ)];
            int candidateMeta = access.getBlockMetadata(x() + dir.offsetX, y() + dir.offsetY, z() + dir.offsetZ);
            boolean flag = true;
            for (ItemStack stack : block.getBlockDropped(getWorldObj(), x(), y(), z(), candidateMeta, 0))
                if (!this.canInsertItem(0, stack, facing))
                {
                    flag = false;
                }
            if (flag)
            {
                getWorldObj().destroyBlock(x() + dir.offsetX, y() + dir.offsetY, z(), false);
            }


        }

        return super.getWeakRedstonePower(access, side);
    }

    @Override
    public void onPlaced (EntityLivingBase entityLiving, ItemStack itemStack)
    {
        super.onPlaced(entityLiving, itemStack);
    }

    @Override
    public void setDirection (ForgeDirection direction)
    {
        super.setDirection(direction);
    }

    @Override
    public ForgeDirection getDirection ()
    {
        return super.getDirection();
    }
}

package com.builtbroken.assemblyline.item;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.builtbroken.minecraft.DarkCore;

/** Creative mod tool to quickly create a large hole in the ground. Is designed only for debug
 * testing of ore generation.
 * 
 * @author DarkGuardsman */
public class ItemInstaHole extends Item
{

    public ItemInstaHole()
    {
        super(DarkCore.getNextItemId());
        this.setUnlocalizedName("InstaHole");
        this.setCreativeTab(CreativeTabs.tabTools);
    }

    @Override
    public boolean onItemUse(ItemStack item, EntityPlayer player, World world, int xx, int yy, int zz, int par7, float par8, float par9, float par10)
    {
        if (player != null && player.capabilities.isCreativeMode)
        {
            if (world.isRemote)
            {
                return true;
            }
            for (int y = yy; y > 0; y--)
            {
                for (int x = xx - 10; x < xx + 10; x++)
                {
                    for (int z = zz - 10; z < zz + 10; z++)
                    {
                        int id = world.getBlockId(x, y, z);
                        if (id == 0)
                        {
                            world.setBlock(x, y, z, 20);
                        }
                        else if (id == Block.sand.blockID || id == Block.gravel.blockID || id == 1 || id == Block.dirt.blockID || id == Block.grass.blockID)
                        {
                            world.setBlock(x, y, z, 0);
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }
}

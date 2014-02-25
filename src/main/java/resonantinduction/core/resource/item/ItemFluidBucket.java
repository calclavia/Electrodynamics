package resonantinduction.core.resource.item;

import resonantinduction.core.Reference;
import resonantinduction.core.TabRI;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

/** Modified version of the MC bucket to meet the needs of a dynamic fluid registry system
 * 
 * @author Darkguardsman */
public class ItemFluidBucket extends ItemBucket
{
    private Fluid fluid;

    public ItemFluidBucket(int id, Fluid fluid)
    {
        super(id, fluid.getBlockID());
        setContainerItem(Item.bucketEmpty);
        setUnlocalizedName(Reference.PREFIX + "Bucket_" + fluid.getName());
        setTextureName(Reference.PREFIX + "Bucket_" + fluid.getName());
        setCreativeTab(CreativeTabs.tabMisc);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack bucket, World world, EntityPlayer player)
    {
        if (fluid == null || Block.blocksList[fluid.getBlockID()] == null)
        {
            return bucket;
        }
        return super.onItemRightClick(bucket, world, player);
    }

    @Override
    public boolean tryPlaceContainedLiquid(World par1World, int par2, int par3, int par4)
    {
        if (fluid == null || Block.blocksList[fluid.getBlockID()] == null)
        {
            return false;
        }
        return super.tryPlaceContainedLiquid(par1World, par2, par3, par4);
    }
}

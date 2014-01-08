package resonantinduction.tool;

import resonantinduction.misc.EntityFarmEgg;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BehaviorDispenseEgg extends BehaviorDefaultDispenseItem
{
    @Override
    public ItemStack dispenseStack(IBlockSource par1IBlockSource, ItemStack par2ItemStack)
    {
        World world = par1IBlockSource.getWorld();
        IPosition iposition = BlockDispenser.getIPositionFromBlockSource(par1IBlockSource);
        EnumFacing enumfacing = BlockDispenser.getFacing(par1IBlockSource.getBlockMetadata());
        IProjectile iprojectile = this.getProjectileEntity(world, iposition, par2ItemStack.getItemDamage());
        iprojectile.setThrowableHeading(enumfacing.getFrontOffsetX(), (enumfacing.getFrontOffsetY() + 0.1F), enumfacing.getFrontOffsetZ(), this.func_82500_b(), this.func_82498_a());
        world.spawnEntityInWorld((Entity) iprojectile);
        par2ItemStack.splitStack(1);
        return par2ItemStack;
    }

    private IProjectile getProjectileEntity(World world, IPosition p, int meta)
    {
        return new EntityFarmEgg(world, p.getX(), p.getY(), p.getZ(), meta);
    }

    @Override
    protected void playDispenseSound(IBlockSource par1IBlockSource)
    {
        par1IBlockSource.getWorld().playAuxSFX(1002, par1IBlockSource.getXInt(), par1IBlockSource.getYInt(), par1IBlockSource.getZInt(), 0);
    }

    protected float func_82498_a()
    {
        return 6.0F;
    }

    protected float func_82500_b()
    {
        return 1.1F;
    }
}

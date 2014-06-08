package resonantinduction.mechanical.energy.turbine;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonant.lib.prefab.block.BlockRotatable;
import resonantinduction.core.Reference;
import resonantinduction.core.resource.ItemHandCrank;

public class BlockTurbine extends BlockRotatable
{
    public BlockTurbine(int id)
    {
        super(id, Material.iron);
        setTextureName(Reference.PREFIX + "material_wood_surface");
        rotationMask = Byte.parseByte("111111", 2);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, int par5, int par6)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity instanceof TileTurbine)
        {
            ((TileTurbine) tileEntity).getMultiBlock().deconstruct();
        }
        dropDamage = getDamageValue(world, x, y, z);
        super.breakBlock(world, x, y, z, par5, par6);
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public int getDamageValue(World world, int x, int y, int z)
    {
        TileEntity tile = world.getBlockTileEntity(x, y, z);

        if (tile instanceof TileTurbine)
            return ((TileTurbine) tile).tier;

        return 0;
    }

    /** Temporarily "cheat" var for dropping with damage. */
    int dropDamage = 0;

    @Override
    public int damageDropped(int par1)
    {
        return dropDamage;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLiving, ItemStack itemStack)
    {
        super.onBlockPlacedBy(world, x, y, z, entityLiving, itemStack);
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity instanceof TileTurbine)
        {
            ((TileTurbine) tileEntity).tier = itemStack.getItemDamage();
        }
    }

    @Override
    public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        if (player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof ItemHandCrank)
        {
            TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

            if (tileEntity instanceof TileTurbine)
            {
                if (!world.isRemote)
                {
                    TileTurbine tile = (TileTurbine) tileEntity;
                    tile.mechanicalNode.torque = -tile.mechanicalNode.torque;
                    tile.mechanicalNode.angularVelocity = -tile.mechanicalNode.angularVelocity;
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity instanceof TileTurbine)
        {
            TileTurbine tile = (TileTurbine) tileEntity;

            if (tile.getMultiBlock().isConstructed())
            {
                tile.getMultiBlock().deconstruct();
                tile.multiBlockRadius++;

                if (!tile.getMultiBlock().construct())
                {
                    tile.multiBlockRadius = 1;
                }

                return true;
            }
            else
            {
                if (!tile.getMultiBlock().construct())
                {
                    tile.multiBlockRadius = 1;
                    tile.getMultiBlock().construct();
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (!world.isRemote && tileEntity instanceof TileTurbine)
        {
            Set<TileTurbine> toFlip = new HashSet<TileTurbine>();

            if (!((TileTurbine) tileEntity).getMultiBlock().isConstructed())
            {
                toFlip.add((TileTurbine) tileEntity);
            }
            else
            {
                Set<TileTurbine> str = ((TileTurbine) tileEntity).getMultiBlock().getPrimary().getMultiBlock().getStructure();

                if (str != null)
                    toFlip.addAll(str);
            }

            for (TileTurbine turbine : toFlip)
            {
                if (side == turbine.getDirection().ordinal())
                    world.setBlockMetadataWithNotify(turbine.xCoord, turbine.yCoord, turbine.zCoord, side ^ 1, 3);
                else
                    world.setBlockMetadataWithNotify(turbine.xCoord, turbine.yCoord, turbine.zCoord, side, 3);
            }
        }

        return true;
    }
}

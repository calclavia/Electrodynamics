package resonantinduction.mechanical.energy.turbine;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonant.lib.prefab.block.BlockRotatable;

/*
 * Turbine block, extend this.
 */
public class BlockTurbineBase extends BlockRotatable
{
    public BlockTurbineBase(int id, Material material)
    {
        super(id, material);
        rotationMask = Byte.parseByte("000001", 2);
    }

    @Override
    public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity instanceof TileTurbine)
        {
            if (!world.isRemote)
            {
                return ((TileTurbine) tileEntity).getMultiBlock().toggleConstruct();
            }

            return true;
        }

        return false;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, int par5, int par6)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity instanceof TileTurbine)
        {
            ((TileTurbine) tileEntity).getMultiBlock().deconstruct();
        }

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
}

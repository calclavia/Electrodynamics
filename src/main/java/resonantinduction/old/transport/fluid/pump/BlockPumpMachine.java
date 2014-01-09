package resonantinduction.old.transport.fluid.pump;

import java.util.List;
import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.Settings;
import resonantinduction.old.client.render.BlockRenderHelper;
import resonantinduction.old.core.recipe.RecipeLoader;
import calclavia.lib.prefab.block.BlockMachine;

import com.builtbroken.common.Pair;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPumpMachine extends BlockMachine
{

    public BlockPumpMachine()
    {
        super(Settings.CONFIGURATION, "StarterPump", Material.iron);
        this.setHardness(1f);
        this.setResistance(5f);
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType()
    {
        return BlockRenderHelper.renderID;
    }

    @Override
    public int damageDropped(int meta)
    {
        return 0;
    }

    @Override
    public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side)
    {
        return side != ForgeDirection.DOWN;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
    {
        int meta = world.getBlockMetadata(x, y, z);
        TileEntity ent = world.getBlockTileEntity(x, y, z);

        if (meta < 4)
        {
            return new ItemStack(RecipeLoader.blockPumpMachine, 1, 0);
        }

        return null;
    }

    @Override
    public TileEntity createNewTileEntity(World world)
    {
        return new TileEntityStarterPump();
    }

    @Override
    public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        par3List.add(new ItemStack(par1, 1, 0));
    }

    @Override
    public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
    {
        list.add(new Pair<String, Class<? extends TileEntity>>("starterPump", TileEntityStarterPump.class));

    }
}

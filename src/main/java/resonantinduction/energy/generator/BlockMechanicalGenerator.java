package resonantinduction.energy.generator;

import java.util.ArrayList;
import java.util.Set;

import resonantinduction.ALRecipeLoader;
import resonantinduction.blocks.BlockHydraulic;
import resonantinduction.client.render.BlockRenderHelper;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import com.builtbroken.common.Pair;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMechanicalGenerator extends BlockHydraulic
{

    public BlockMechanicalGenerator()
    {
        super("MechanicalGenerator", Material.iron);
        this.setHardness(1f);
        this.setResistance(5f);
    }

    @Override
    public void addCreativeItems(ArrayList itemList)
    {
        itemList.add(new ItemStack(this, 1, 0));
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
    {

        return new ItemStack(ALRecipeLoader.blockGenerator, 1);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase par5EntityLiving, ItemStack stack)
    {
        int angle = MathHelper.floor_double((par5EntityLiving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        world.setBlockMetadataWithNotify(x, y, z, blockID, angle);
    }

    @Override
    public boolean onUseWrench(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        int angle = MathHelper.floor_double((par5EntityPlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int metadata = par1World.getBlockMetadata(x, y, z);
        if (metadata < 3)
        {
            par1World.setBlockMetadataWithNotify(x, y, z, blockID, metadata + angle);
        }
        else
        {
            par1World.setBlockMetadataWithNotify(x, y, z, blockID, 0);
        }
        return true;
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
    public void onNeighborBlockChange(World par1World, int x, int y, int z, int side)
    {
        super.onNeighborBlockChange(par1World, x, y, z, side);

    }

    @Override
    public TileEntity createNewTileEntity(World world)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
    {
        // TODO Auto-generated method stub

    }
}

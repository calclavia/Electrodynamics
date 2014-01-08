package resonantinduction.energy.generator;

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
import net.minecraftforge.common.ForgeDirection;

import com.builtbroken.common.Pair;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockRod extends BlockHydraulic
{

    public BlockRod()
    {
        super("MechanicalRod", Material.iron);
        this.setHardness(1f);
        this.setResistance(5f);
    }

    @Override
    public int damageDropped(int metadata)
    {
        return 0;
    }

    @Override
    public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase player, ItemStack stack)
    {
        int angle = MathHelper.floor_double((player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int meta = 0;
        ForgeDirection idr;
        int dZ = 0;
        int dX = 0;
        switch (angle)
        {
            case 0:
                meta = 2;
                dZ--;
                break;
            case 1:
                meta = 5;
                dX--;
                break;
            case 2:
                meta = 3;
                dZ++;
                break;
            case 3:
                meta = 4;
                dX++;
                break;
        }
        world.setBlockMetadataWithNotify(i, j, k, blockID, meta);
    }

    @Override
    public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
        int meta = world.getBlockMetadata(x, y, z);
        if (meta >= 5)
        {
            world.setBlockMetadataWithNotify(x, y, z, 0, 3);
        }
        else
        {
            world.setBlockMetadataWithNotify(x, y, z, meta + 1, 3);
        }
        return true;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
    {
        return new ItemStack(ALRecipeLoader.blockRod, 1, 0);
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

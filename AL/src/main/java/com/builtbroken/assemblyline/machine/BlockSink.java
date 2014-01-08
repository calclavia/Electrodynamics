package com.builtbroken.assemblyline.machine;

import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import com.builtbroken.assemblyline.blocks.BlockHydraulic;
import com.builtbroken.assemblyline.client.render.BlockRenderHelper;
import com.builtbroken.common.Pair;
import com.builtbroken.minecraft.FluidHelper;
import com.builtbroken.minecraft.prefab.TileEntityAdvanced;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockSink extends BlockHydraulic
{
    public BlockSink()
    {
        super("FluidSink", Material.iron);
        this.setResistance(4f);
        this.setHardness(4f);
    }

    @Override
    public TileEntity createNewTileEntity(World var1)
    {
        return new TileEntitySink();
    }

    @Override
    public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int side, float hitX, float hitY, float hitZ)
    {
        return FluidHelper.playerActivatedFluidItem(world, x, y, z, entityplayer, side);
    }

    @Override
    public boolean onUseWrench(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        int meta = par1World.getBlockMetadata(x, y, z);
        int metaGroup = meta % 4;

        if (meta == (metaGroup * 4) + 3)
        {
            par1World.setBlockMetadataWithNotify(x, y, z, (metaGroup * 4), 3);
            return true;
        }
        else
        {
            par1World.setBlockMetadataWithNotify(x, y, z, meta + 1, 3);
            return true;
        }
        // return false;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase par5EntityLiving, ItemStack itemStack)
    {
        int meta = world.getBlockMetadata(x, y, z);
        int angle = MathHelper.floor_double((par5EntityLiving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        TileEntity ent = world.getBlockTileEntity(x, y, z);

        world.setBlockMetadataWithNotify(x, y, z, angle * 4, 3);
        if (ent instanceof TileEntityAdvanced)
        {
            ((TileEntityAdvanced) world.getBlockTileEntity(x, y, z)).initiate();
        }

        world.notifyBlocksOfNeighborChange(x, y, z, this.blockID);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
    {
        int meta = world.getBlockMetadata(x, y, z);

        return new ItemStack(this, 1, 0);

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
    public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
    {
        list.add(new Pair<String, Class<? extends TileEntity>>("FluidSink", TileEntitySink.class));

    }
}

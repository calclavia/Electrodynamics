package com.builtbroken.assemblyline.fluid.pipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import universalelectricity.api.vector.Vector3;

import com.builtbroken.assemblyline.blocks.BlockHydraulic;
import com.builtbroken.common.Pair;
import com.builtbroken.minecraft.FluidHelper;
import com.builtbroken.minecraft.helpers.ColorCode;
import com.builtbroken.minecraft.helpers.ColorCode.IColorCoded;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPipe extends BlockHydraulic
{

    public static int waterFlowRate = 3000;

    public BlockPipe()
    {
        super("FluidPipe", Material.iron);
        this.setBlockBounds(0.30F, 0.30F, 0.30F, 0.70F, 0.70F, 0.70F);
        this.setHardness(1f);
        this.setResistance(3f);

    }

    @Override
    public void fillWithRain(World world, int x, int y, int z)
    {
        int meta = world.getBlockMetadata(x, y, z);
        if (meta == FluidPartsMaterial.WOOD.ordinal() || meta == FluidPartsMaterial.STONE.ordinal())
        {
            //TODO fill pipe since it will have an open top and can gather rain
        }
    }

    @Override
    public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune)
    {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
        TileEntity entity = world.getBlockTileEntity(x, y, z);
        if (entity instanceof TileEntityPipe)
        {
            ret.add(new ItemStack(this, 1, FluidPartsMaterial.getDropItemMeta(world, x, y, z)));
        }
        return ret;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType()
    {
        return -1;
    }

    @Override
    public TileEntity createNewTileEntity(World var1)
    {
        return new TileEntityPipe();
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
    {
        return new ItemStack(this, 1, FluidPartsMaterial.getDropItemMeta(world, x, y, z));
    }

    @Override
    public boolean canSilkHarvest(World world, EntityPlayer player, int x, int y, int z, int metadata)
    {
        return false;
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z)
    {
        if (world.getBlockMetadata(x, y, z) == FluidPartsMaterial.HELL.ordinal())
        {
            return 5;
        }
        return super.getLightValue(world, x, y, z);
    }

    @Override
    public boolean isLadder(World world, int x, int y, int z, EntityLivingBase entity)
    {
        return true;
    }

    @Override
    public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (FluidPartsMaterial data : FluidPartsMaterial.values())
        {
            par3List.add(data.getStack());
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, int par5, int par6)
    {
        TileEntity entity = world.getBlockTileEntity(x, y, z);
        if (entity instanceof TileEntityPipe)
        {
            FluidTankInfo tank = ((TileEntityPipe) entity).getTankInfo()[0];
            if (tank != null && tank.fluid != null && tank.fluid.getFluid() != null && tank.fluid.amount > 0)
            {
                ((TileEntityPipe) entity).getTileNetwork().drainNetworkTank(world, FluidHelper.fillBlock(world, new Vector3(x, y, z), tank.fluid, true), true);
            }
        }
        super.breakBlock(world, x, y, z, par5, par6);

    }

    @Override
    public boolean recolourBlock(World world, int x, int y, int z, ForgeDirection side, int colour)
    {
        if (world.getBlockTileEntity(x, y, z) instanceof IColorCoded)
        {
            return ((IColorCoded) world.getBlockTileEntity(x, y, z)).setColor(ColorCode.get(colour));
        }
        return false;
    }

    @Override
    public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
    {
        list.add(new Pair<String, Class<? extends TileEntity>>("FluidPipe", TileEntityPipe.class));
        list.add(new Pair<String, Class<? extends TileEntity>>("ColoredPipe", TileEntityPipe.class));
    }

    @Override
    public boolean hasExtraConfigs()
    {
        return true;
    }

    @Override
    public void loadExtraConfigs(Configuration config)
    {
        BlockPipe.waterFlowRate = config.get("settings", "FlowRate", BlockPipe.waterFlowRate, "Base value for flow rate is based off of water. It is in milibuckets so 1000 equals one bucket of fluid").getInt();

    }
}

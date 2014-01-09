package resonantinduction.transport.fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import resonantinduction.client.render.BlockRenderHelper;
import resonantinduction.core.Settings;
import resonantinduction.transport.fluid.pipes.FluidPartsMaterial;
import resonantinduction.transport.fluid.pipes.ItemBlockPipe;
import resonantinduction.transport.fluid.pipes.TileEntityPipe;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.FluidHelper;
import calclavia.lib.utility.HelperMethods;

import com.builtbroken.common.Pair;
import com.builtbroken.minecraft.prefab.BlockMachine;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockTank extends BlockMachine
{
    public static int tankVolume = 16;

    public BlockTank()
    {
        super(Settings.CONFIGURATION, "FluidTank", Material.rock);
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
    public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int side, float hitX, float hitY, float hitZ)
    {
        return FluidHelper.playerActivatedFluidItem(world, x, y, z, entityplayer, side);
    }

    @Override
    public TileEntity createNewTileEntity(World var1)
    {
        return new TileEntityTank();
    }

    @Override
    public boolean hasComparatorInputOverride()
    {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int par5)
    {
        TileEntityTank tileEntity = (TileEntityTank) world.getBlockTileEntity(x, y, z);
        if (tileEntity != null && tileEntity.getTileNetwork().getNetworkTankInfo().fluid != null)
        {
            return 15 * (tileEntity.getTileNetwork().getNetworkTankInfo().fluid.amount / tileEntity.getTileNetwork().getNetworkTankInfo().capacity);
        }
        return 0;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
    {
        return new ItemStack(this, 1, FluidPartsMaterial.getDropItemMeta(world, x, y, z));
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

    @SuppressWarnings("unchecked")
    @Override
    public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (FluidPartsMaterial data : FluidPartsMaterial.values())
        {
            par3List.add(new ItemStack(this, 1, data.ordinal() * FluidPartsMaterial.spacing));
        }
    }

    @Override
    public boolean onSneakMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote)
        {
            ItemStack dropStack = ItemBlockPipe.getWrenchedItem(world, new Vector3(x, y, z));
            if (dropStack != null)
            {
                if (entityPlayer.getHeldItem() == null)
                {
                    entityPlayer.inventory.setInventorySlotContents(entityPlayer.inventory.currentItem, dropStack);
                }
                else
                {
                    HelperMethods.dropItemStack(world, new Vector3(x, y, z), dropStack, false);
                }
                world.setBlockToAir(x, y, z);
            }
        }
        return true;
    }

    @Override
    public void loadExtraConfigs(Configuration config)
    {
        BlockTank.tankVolume = config.get("settings", "TankBucketVolume", 16, "Number of buckets each tank block can store, Settings this to zero is the same as one").getInt();

    }

    @Override
    public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
    {
        list.add(new Pair<String, Class<? extends TileEntity>>("FluidTank", TileEntityTank.class));

    }
}

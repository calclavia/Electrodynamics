package resonantinduction.core.debug;

import java.util.List;
import java.util.Set;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.block.BlockRI;
import calclavia.lib.content.IBlockInfo;

import com.builtbroken.common.Pair;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockDebug extends BlockRI implements IBlockInfo
{
    public static float DebugWattOut, DebugWattDemand;

    public BlockDebug()
    {
        super("DebugBlock");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister iconReg)
    {
        super.registerIcons(iconReg);
        for (DebugBlocks block : DebugBlocks.values())
        {
            block.icon = iconReg.registerIcon(Reference.PREFIX + block.getTextureName());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int meta)
    {
        if (meta < DebugBlocks.values().length)
        {
            return DebugBlocks.values()[meta].icon;
        }
        return this.blockIcon;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata)
    {
        if (metadata < DebugBlocks.values().length)
        {
            try
            {
                return DebugBlocks.values()[metadata].clazz.newInstance();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return super.createTileEntity(world, metadata);
    }

    @Override
    public TileEntity createNewTileEntity(World world)
    {
        return null;
    }

    @Override
    public void getSubBlocks(int blockID, CreativeTabs tab, List creativeTabList)
    {
        for (DebugBlocks block : DebugBlocks.values())
        {
            creativeTabList.add(new ItemStack(blockID, 1, block.ordinal()));
        }
    }

    @Override
    public void getTileEntities(int blockID, Set<Pair<String, Class<? extends TileEntity>>> list)
    {
        for (DebugBlocks block : DebugBlocks.values())
        {
            list.add(new Pair<String, Class<? extends TileEntity>>(block.name, block.clazz));

        }
    }

    @Override
    public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        return false;
    }

    @Override
    public boolean onSneakMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        return false;
    }

    @Override
    public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        return false;
    }

    @Override
    public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        return this.onUseWrench(world, x, y, z, entityPlayer, side, hitX, hitY, hitZ);
    }

    public static enum DebugBlocks
    {
        SOURCE("UnlimitedPower", TileInfiniteEnergySource.class, "infSource"),
        FLUID("UnlimitedFluid", TileInfiniteFluidSource.class, "infFluid"),
        VOID("FluidVoid", TileInfiniteFluidSink.class, "void"),
        LOAD("PowerVampire", TileInfiniteEnergySink.class, "infLoad");
        public Icon icon;
        public String name;
        public String texture;
        Class<? extends TileEntity> clazz;

        private DebugBlocks(String name, Class<? extends TileEntity> clazz)
        {
            this.name = name;
            this.clazz = clazz;
        }

        private DebugBlocks(String name, Class<? extends TileEntity> clazz, String texture)
        {
            this(name, clazz);
            this.texture = texture;
        }

        public String getTextureName()
        {
            if (texture == null || texture.isEmpty())
            {
                return name;
            }
            return texture;
        }

    }

    @Override
    public void getClientTileEntityRenderers(List<Pair<Class<? extends TileEntity>, TileEntitySpecialRenderer>> list)
    {
        // TODO Auto-generated method stub

    }

}

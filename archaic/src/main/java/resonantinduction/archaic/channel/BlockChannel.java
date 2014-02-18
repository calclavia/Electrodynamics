package resonantinduction.archaic.channel;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import resonantinduction.core.prefab.fluid.BlockFluidNetwork;
import resonantinduction.core.render.RIBlockRenderingHandler;
import universalelectricity.api.UniversalElectricity;
import calclavia.lib.utility.FluidUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Early tier version of the basic pipe. Open on the top, and can't support pressure.
 * 
 * @author Darkguardsman */
public class BlockChannel extends BlockFluidNetwork
{
    public BlockChannel(int id)
    {
        super(id, UniversalElectricity.machine);
        setTextureName(Reference.PREFIX + "material_wood_surface");
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
    public TileEntity createNewTileEntity(World world)
    {
        return new TileChannel();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType()
    {
        return RIBlockRenderingHandler.ID;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int side, float hitX, float hitY, float hitZ)
    {
        TileEntity tile = world.getBlockTileEntity(x, y, z);
        if (!world.isRemote && tile instanceof TileChannel)
        {
            if (!((TileChannel) tile).onActivated(entityplayer))
            {
                return FluidUtility.playerActivatedFluidItem(world, x, y, z, entityplayer, side);
            }
        }
        return true;
    }
}

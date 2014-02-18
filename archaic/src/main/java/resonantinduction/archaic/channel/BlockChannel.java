package resonantinduction.archaic.channel;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import calclavia.lib.utility.FluidUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import resonantinduction.core.Settings;
import resonantinduction.core.prefab.fluid.BlockFluidNetwork;
import resonantinduction.core.render.RIBlockRenderingHandler;
import universalelectricity.api.UniversalElectricity;

/** Early tier version of the basic pipe. Open on the top, and can't support pressure.
 * 
 * @author Darkguardsman */
public class BlockChannel extends BlockFluidNetwork
{
    public BlockChannel(int id)
    {
        super(Settings.CONFIGURATION.getBlock("Channel", id).getInt(id), UniversalElectricity.machine);
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
        return -1;
    }

    @Override
    public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int side, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote)
        {
            return FluidUtility.playerActivatedFluidItem(world, x, y, z, entityplayer, side);
        }
        return super.onMachineActivated(world, x, y, z, entityplayer, side, hitX, hitY, hitZ);
    }

}

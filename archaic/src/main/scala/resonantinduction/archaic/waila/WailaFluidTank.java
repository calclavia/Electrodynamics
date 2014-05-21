package resonantinduction.archaic.waila;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidTank;
import resonant.lib.utility.LanguageUtility;
import resonantinduction.archaic.fluid.tank.TileTank;

/** Waila support for tanks
 * 
 * @author Darkguardsman */
public class WailaFluidTank implements IWailaDataProvider
{
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        TileEntity tile = accessor.getTileEntity();
        if (tile instanceof TileTank)
        {
            FluidTank tank = ((TileTank) tile).getInternalTank();
            if (tank != null && tank.getFluid() != null)
            {
                currenttip.add(LanguageUtility.getLocal("info.waila.tank.fluid") + " " + tank.getFluid().getFluid().getLocalizedName());
                currenttip.add(LanguageUtility.getLocal("info.waila.tank.vol") + " " + tank.getFluidAmount() + " / " + tank.getCapacity());
            }
        }
        return currenttip;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return currenttip;
    }

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return null;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        return currenttip;
    }

}

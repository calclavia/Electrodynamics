package resonantinduction.archaic.waila;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import resonant.lib.utility.LanguageUtility;
import resonantinduction.archaic.crate.TileCrate;

/** Waila support for crates
 * 
 * @author Darkguardsman */
public class WailaCrate implements IWailaDataProvider
{
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
    {
        TileEntity tile = accessor.getTileEntity();
        if (tile instanceof TileCrate)
        {
            ItemStack stored = ((TileCrate) tile).getSampleStack();
            int cap = ((TileCrate) tile).getSlotCount() * 64;
            if (stored != null)
            {
                currenttip.add(LanguageUtility.getLocal("info.waila.crate.stack") + " " + stored.getDisplayName());
                currenttip.add(LanguageUtility.getLocal("info.waila.crate.stored") + " " + stored.stackSize + " / " + cap);
            }
            else
            {
                currenttip.add(LanguageUtility.getLocal("info.waila.crate.empty"));
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

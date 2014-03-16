package resonantinduction.electrical.itemrailing;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.prefab.part.PartFramedConnection;
import resonantinduction.electrical.Electrical;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyNetwork;
import universalelectricity.api.net.IConnector;

/**
 * @since 16/03/14
 * @author tgame14
 */
public class PartRailing extends PartFramedConnection<PartRailing.EnumRailing, IConductor, IEnergyNetwork>
{
    public enum EnumRailing
    {
        DEFAULT;
    }

    public PartRailing()
    {
        super(Electrical.itemInsulation);
    }

    @Override
    protected boolean canConnectTo (TileEntity tile, ForgeDirection to)
    {
        return false;
    }

    @Override
    protected IConnector getConnector (TileEntity tile)
    {
        return null;
    }

    @Override
    public Object getNetwork ()
    {
        return null;
    }

    @Override
    public void setNetwork (Object network)
    {

    }

    @Override
    public void setMaterial (int i)
    {

    }

    @Override
    protected ItemStack getItem ()
    {
        return null;
    }

    @Override
    public String getType ()
    {
        return null;
    }
}

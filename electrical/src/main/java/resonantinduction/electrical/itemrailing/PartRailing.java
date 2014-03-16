package resonantinduction.electrical.itemrailing;

import codechicken.microblock.IHollowConnect;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TSlottedPart;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.prefab.part.PartFramedConnection;
import resonantinduction.electrical.Electrical;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyNetwork;

/**
 * @since 16/03/14
 * @author tgame14
 */
public class PartRailing extends PartFramedConnection<PartRailing.EnumRailing, IConductor, IEnergyNetwork> implements IConductor, TSlottedPart, JNormalOcclusion, IHollowConnect
{
    @Override
    public float getResistance ()
    {
        return 0;
    }

    @Override
    public long getCurrentCapacity ()
    {
        return 0;
    }

    @Override
    public long onReceiveEnergy (ForgeDirection from, long receive, boolean doReceive)
    {
        return 0;
    }

    @Override
    public long onExtractEnergy (ForgeDirection from, long extract, boolean doExtract)
    {
        return 0;
    }

    public enum EnumRailing
    {
        DEFAULT;
    }

    public PartRailing()
    {
        super(Electrical.itemInsulation);
    }

    @Override
    public boolean doesTick ()
    {
        return false;
    }

    @Override
    protected boolean canConnectTo (TileEntity tile, ForgeDirection to)
    {
        return false;
    }

    @Override
    protected IConductor getConnector (TileEntity tile)
    {
        return tile instanceof IConductor ? (IConductor) ((IConductor) tile).getInstance(ForgeDirection.UNKNOWN) : null;
    }

    @Override
    public IEnergyNetwork getNetwork ()
    {
        return null;
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
        return "resonant_induction_itemrailing";
    }

  }

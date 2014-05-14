package resonantinduction.electrical.itemrailing;

import java.lang.reflect.Constructor;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.grid.INode;
import resonantinduction.core.prefab.part.PartFramedConnection;
import resonantinduction.electrical.Electrical;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailingProvider;
import universalelectricity.api.energy.EnergyNetworkLoader;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyNetwork;
import universalelectricity.api.vector.VectorWorld;
import codechicken.multipart.TileMultipart;

/**
 * @since 16/03/14
 * @author tgame14
 */
public class PartRailing extends PartFramedConnection<PartRailing.EnumRailing, IConductor, IEnergyNetwork> implements IConductor, IItemRailingProvider
{

    public static enum EnumRailing
    {
        DEFAULT, EXTENTION;
    }

	public NodeRailing getNode()
	{
		return node;
	}

    private NodeRailing node;

    public PartRailing ()
    {
        super(Electrical.itemInsulation);
		this.material = EnumRailing.DEFAULT;
		this.node = new NodeRailing(this);
    }


    @Override
    public <N extends INode> N getNode (Class<? super N> nodeType, ForgeDirection from)
    {
        if (nodeType.isInstance(this.node))
            return (N) node;
        try
        {
            for (Constructor con : nodeType.getConstructors())
            {
                if ((con.getParameterTypes().length == 1) && con.getParameterTypes()[0].equals(getClass()))
                {
                    this.node = (NodeRailing) con.newInstance(this);
                    return (N) this.node;
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

	public VectorWorld getWorldPos()
	{
		return new VectorWorld(getWorld(), x(), y(), z());
	}

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

    @Override
    public boolean doesTick ()
    {
        return false;
    }

    @Override
    protected boolean canConnectTo (TileEntity tile, ForgeDirection to)
    {
		Object obj = tile instanceof TileMultipart ? ((TileMultipart) tile).partMap(ForgeDirection.UNKNOWN.ordinal()) : tile;
		return obj instanceof IInventory ? true : obj instanceof PartRailing;
    }

    @Override
    protected IConductor getConnector (TileEntity tile)
    {
        return tile instanceof IConductor ? (IConductor) ((IConductor) tile).getInstance(ForgeDirection.UNKNOWN) : null;
    }

	@Override
	public IEnergyNetwork getNetwork()
	{
		if (network == null)
		{
			setNetwork(EnergyNetworkLoader.getNewNetwork(this));
		}

		return network;
	}

	//TODO: Fix up to proper data
    @Override
    public void setMaterial (int i)
    {
		this.material = EnumRailing.values()[i];
    }

    @Override
    protected ItemStack getItem ()
    {
        return new ItemStack(Electrical.itemRailing);
    }

    @Override
    public String getType ()
    {
        return "resonant_induction_itemrailing";
    }

}

package resonantinduction.electrical.itemrailing;

import calclavia.lib.grid.INode;
import calclavia.lib.grid.INodeProvider;
import calclavia.lib.render.EnumColor;
import codechicken.microblock.IHollowConnect;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TSlottedPart;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.prefab.part.PartFramedConnection;
import resonantinduction.electrical.Electrical;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailing;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailingTransfer;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyNetwork;
import universalelectricity.api.vector.IVectorWorld;
import universalelectricity.api.vector.VectorWorld;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @since 16/03/14
 * @author tgame14
 */
public class PartRailing extends PartFramedConnection<PartRailing.EnumRailing, IConductor, IEnergyNetwork> implements IConductor, TSlottedPart, JNormalOcclusion, IHollowConnect, IItemRailing
{

    public static enum EnumRailing
    {
        DEFAULT, EXTENTION;
    }

    // default is NULL
    private EnumColor color;
    private NodeRailing node;

    public PartRailing ()
    {
        super(Electrical.itemInsulation);

        this.color = null;
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

    @Override
    public boolean canItemEnter (IItemRailingTransfer item)
    {
        return this.color != null ? this.color == item.getColor() : false;
    }

    @Override
    public boolean canConnectToRailing (IItemRailing railing, ForgeDirection from)
    {
        return this.color != null ? this.color == railing.getRailingColor() : true;
    }

    @Override
    public EnumColor getRailingColor ()
    {
        return this.color;
    }

    @Override
    public IItemRailing setRailingColor (EnumColor color)
    {
        this.color = color;
        return this;
    }

	@Override
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
        return tile instanceof IItemRailing ? canConnectToRailing((IItemRailing) tile, to) : tile instanceof IInventory ? true : false;
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

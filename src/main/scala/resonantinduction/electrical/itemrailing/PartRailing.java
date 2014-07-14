package resonantinduction.electrical.itemrailing;

import java.lang.reflect.Constructor;

import codechicken.lib.vec.Vector3;
import codechicken.microblock.IHollowConnect;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TSlottedPart;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.IExternalInventory;
import resonant.api.IExternalInventoryBox;
import resonant.api.grid.INode;
import resonant.lib.utility.inventory.ExternalInventory;
import resonantinduction.core.prefab.part.PartFramedNode;
import resonantinduction.electrical.Electrical;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailingProvider;
import universalelectricity.api.energy.EnergyNetworkLoader;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyNetwork;
import universalelectricity.api.net.IConnector;
import universalelectricity.api.net.INetwork;
import universalelectricity.api.vector.IVectorWorld;
import universalelectricity.api.vector.VectorWorld;

/**
 * @since 16/03/14
 * @author tgame14
 */
public class PartRailing extends PartFramedNode<EnumRailingMaterial, NodeRailing, IItemRailingProvider> implements IItemRailingProvider, TSlottedPart, JNormalOcclusion, IHollowConnect, IExternalInventory
{
    protected ExternalInventory inventory;
    protected boolean markPacketUpdate;

    public PartRailing ()
    {
        super(Electrical.itemInsulation);
		this.material = EnumRailingMaterial.DEFAULT;
		this.node = new NodeRailing(this);
        this.markPacketUpdate = true;
        this.requiresInsulation = false;
        this.inventory = new ExternalInventory(tile(), 5);
    }


    @Override
    public INode getNode(Class<? extends INode> nodeType, ForgeDirection from)
    {
        if (nodeType.isInstance(this.node))
            return node;
        try
        {
            for (Constructor con : nodeType.getConstructors())
            {
                if ((con.getParameterTypes().length == 1) && con.getParameterTypes()[0].equals(getClass()))
                {
                    this.node = (NodeRailing) con.newInstance(this);
                    return this.node;
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
    public boolean doesTick ()
    {
        return false;
    }

	//TODO: Fix up to proper data
    @Override
    public void setMaterial (int i)
    {
		this.material = EnumRailingMaterial.values()[i];
    }

    @Override
    protected ItemStack getItem ()
    {
        return new ItemStack(Electrical.itemRailing, 1, getMaterialID());
    }

    @Override
    public String getType ()
    {
        return "resonant_induction_itemrailing";
    }

    @Override
    public void renderDynamic (Vector3 pos, float frame, int pass)
    {
        super.renderDynamic(pos, frame, pass);
        //TODO: Implement
    }



    @Override
    public IVectorWorld getVectorWorld ()
    {
        return new VectorWorld(getWorld(), x(), y(), z());
    }

    @Override
    public void onInventoryChanged ()
    {
        //TODO: Implement
    }

    @Override
    public IExternalInventoryBox getInventory ()
    {
        return this.inventory;
    }

    @Override
    public boolean canStore (ItemStack stack, int slot, ForgeDirection side)
    {
        return false;
    }

    @Override
    public boolean canRemove (ItemStack stack, int slot, ForgeDirection side)
    {
        return false;
    }
}

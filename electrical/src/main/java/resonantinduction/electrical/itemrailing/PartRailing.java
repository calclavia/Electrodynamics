package resonantinduction.electrical.itemrailing;

import calclavia.lib.grid.INode;
import calclavia.lib.render.EnumColor;
import codechicken.microblock.IHollowConnect;
import codechicken.multipart.JNormalOcclusion;
import codechicken.multipart.TSlottedPart;
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

/**
 * @since 16/03/14
 * @author tgame14
 */
public class PartRailing extends PartFramedConnection<PartRailing.EnumRailing, IConductor, IEnergyNetwork> implements IConductor, TSlottedPart, JNormalOcclusion, IHollowConnect, IItemRailing, INode
{

    public enum EnumRailing
    {
        DEFAULT;
    }

    // default is NULL
    private EnumColor color;

    public PartRailing ()
    {
        super(Electrical.itemInsulation);

        this.color = null;
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
    public World getWorldObj ()
    {
        return super.getWorld();
    }

    @Override
    public void reconstruct ()
    {

    }

    @Override
    public void deconstruct ()
    {

    }

    @Override
    public void recache ()
    {

    }

    @Override
    public void update (float deltaTime)
    {

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
        if (tile instanceof IItemRailing)
            return canConnectToRailing((IItemRailing) tile, to);
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

package resonantinduction.core.grid.fluid;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import resonant.lib.utility.WorldUtility;
import universalelectricity.api.vector.Vector3;

/** A prefab class for tiles that use the fluid network.
 * 
 * @author DarkGuardsman */
public abstract class TileFluidDistribution extends TileFluidNode implements IFluidDistribution
{

    protected Object[] connectedBlocks = new Object[6];

    /** Network used to link all parts together */
    protected FluidDistributionetwork network;

    public TileFluidDistribution(Material material, int tankSize)
    {
        super(material, tankSize);
    }

    @Override
    public void initiate()
    {
        super.initiate();
        refresh();
        getNetwork().reconstruct();
    }

    @Override
    protected void onNeighborChanged()
    {
        refresh();
    }

    @Override
    public void invalidate()
    {
        this.getNetwork().split(this);
        super.invalidate();
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        return getNetwork().fill(this, from, resource, doFill);
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        return getNetwork().drain(this, from, resource, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
        return getNetwork().drain(this, from, maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid)
    {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)
    {
        return new FluidTankInfo[] { getNetwork().getTank().getInfo() };
    }

    @Override
    public Object[] getConnections()
    {
        return connectedBlocks;
    }

    public void refresh()
    {
        if (this.worldObj != null && !this.worldObj.isRemote)
        {
            byte previousConnections = renderSides;
            connectedBlocks = new Object[6];
            renderSides = 0;

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
            {
                this.validateConnectionSide(new Vector3(this).translate(dir).getTileEntity(worldObj), dir);
            }

            /** Only send packet updates if visuallyConnected changed. */
            if (previousConnections != renderSides)
            {
                getNetwork().update();
                getNetwork().reconstruct();
                sendRenderUpdate();
            }
        }

    }

    /** Checks to make sure the connection is valid to the tileEntity
     * 
     * @param tileEntity - the tileEntity being checked
     * @param side - side the connection is too */
    public void validateConnectionSide(TileEntity tileEntity, ForgeDirection side)
    {
        if (!this.worldObj.isRemote)
        {
            if (tileEntity instanceof IFluidDistribution)
            {
                this.getNetwork().merge(((IFluidDistribution) tileEntity).getNetwork());
                renderSides = WorldUtility.setEnableSide(renderSides, side, true);
                connectedBlocks[side.ordinal()] = tileEntity;
            }
        }
    }

    public int getSubID()
    {
        return this.colorID;
    }

    public void setSubID(int id)
    {
        this.colorID = id;
    }

    @Override
    public boolean canConnect(ForgeDirection direction, Object obj)
    {
        return true;
    }

    @Override
    public IFluidDistribution getInstance(ForgeDirection from)
    {
        return this;
    }
}

package resonantinduction.mechanical.fluid.tank;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import resonantinduction.api.fluid.IFluidNetwork;
import resonantinduction.api.fluid.IFluidPart;
import resonantinduction.mechanical.fluid.prefab.TileFluidNetwork;

public class TileTank extends TileFluidNetwork
{
    public static final int VOLUME = 16;

    public TileTank()
    {
        this.getInternalTank().setCapacity(VOLUME * FluidContainerRegistry.BUCKET_VOLUME);
    }

    @Override
    public TankNetwork getNetwork()
    {
        if (this.network == null)
        {
            this.network = new TankNetwork();
            this.network.addConnector(this);
        }
        return (TankNetwork) this.network;
    }

    @Override
    public void setNetwork(IFluidNetwork network)
    {
        if (network instanceof TankNetwork)
        {
            this.network = (TankNetwork) network;
        }
    }

    @Override
    public void validateConnectionSide(TileEntity tileEntity, ForgeDirection side)
    {
        if (!this.worldObj.isRemote)
        {
            if (tileEntity instanceof TileTank)
            {
                this.getNetwork().merge(((IFluidPart) tileEntity).getNetwork());
                this.setRenderSide(side, true);
                connectedBlocks[side.ordinal()] = tileEntity;
            }
        }
    }

}

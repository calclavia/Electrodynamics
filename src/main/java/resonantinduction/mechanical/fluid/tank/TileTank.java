package resonantinduction.mechanical.fluid.tank;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.api.fluid.IFluidNetwork;
import resonantinduction.api.fluid.IFluidPart;
import resonantinduction.mechanical.fluid.prefab.TileFluidNetwork;

public class TileTank extends TileFluidNetwork
{
    public TileTank()
    {
        super(BlockTank.tankVolume);
    }

    @Override
    public TankNetwork getNetwork()
    {
        if (!(this.network instanceof TankNetwork))
        {
            this.setNetwork(new TankNetwork(this));
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
                if (this.canTileConnect(Connection.NETWORK, side.getOpposite()))
                {
                    this.getNetwork().merge(((IFluidPart) tileEntity).getNetwork());
                    this.setRenderSide(side, true);
                    connectedBlocks[side.ordinal()] = tileEntity;
                }
            }
        }
    }
}

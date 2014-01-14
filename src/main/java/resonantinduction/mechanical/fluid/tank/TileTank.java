package resonantinduction.mechanical.fluid.tank;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.api.IReadOut;
import resonantinduction.api.fluid.IFluidNetwork;
import resonantinduction.api.fluid.IFluidPart;
import resonantinduction.mechanical.fluid.prefab.TileFluidNetwork;

public class TileTank extends TileFluidNetwork implements IReadOut
{
    public static final int VOLUME = 16;

    public TileTank()
    {
        super(VOLUME);
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
                if (this.canTileConnect(Connection.NETWORK, side.getOpposite()))
                {
                    this.getNetwork().merge(((IFluidPart) tileEntity).getNetwork());
                    this.setRenderSide(side, true);
                    connectedBlocks[side.ordinal()] = tileEntity;
                }
            }
        }
    }

    @Override
    public String getMeterReading(EntityPlayer user, ForgeDirection side, EnumTools tool)
    {
        if (tool == EnumTools.PIPE_GUAGE)
        {
            return this.getNetwork().toString();
        }
        return null;
    }
}

package resonantinduction.transport.fluid;

import resonantinduction.api.fluid.INetworkPipe;
import resonantinduction.fluid.network.NetworkPipes;
import resonantinduction.fluid.prefab.TileEntityFluidDevice;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import com.builtbroken.minecraft.helpers.HelperMethods;
import com.builtbroken.minecraft.interfaces.IToolReadOut;
import com.builtbroken.minecraft.tilenetwork.ITileConnector;

public class TileEntityReleaseValve extends TileEntityFluidDevice implements ITileConnector, IToolReadOut
{
    public TileEntity[] connected = new TileEntity[6];

    @Override
    public void updateEntity()
    {
        super.updateEntity();

        if (this.ticks % 10 == 0)
        {
            this.refresh();
            if (!this.worldObj.isRemote && !worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
            {
                for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
                {
                    if (connected[dir.ordinal()] instanceof IFluidHandler && !(connected[dir.ordinal()] instanceof INetworkPipe))
                    {
                        IFluidHandler drainedTank = (IFluidHandler) connected[dir.ordinal()];
                        FluidStack stack = drainedTank.drain(dir.getOpposite(), FluidContainerRegistry.BUCKET_VOLUME, false);
                        if (stack != null && stack.amount > 0)
                        {
                            INetworkPipe inputPipe = this.findValidPipe(stack);
                            if (inputPipe != null)
                            {
                                int ammountFilled = ((NetworkPipes) inputPipe.getTileNetwork()).addFluidToNetwork((TileEntity) drainedTank, stack, true);
                                drainedTank.drain(dir.getOpposite(), ammountFilled, true);
                            }
                        }
                    }
                }
            }
        }
    }

    /** used to find a valid pipe for filling of the liquid type */
    public INetworkPipe findValidPipe(FluidStack stack)
    {
        // find normal color selective pipe first
        for (int i = 0; i < connected.length; i++)
        {
            TileEntity tile = connected[i];
            if (tile instanceof INetworkPipe && ((INetworkPipe) tile).fill(ForgeDirection.getOrientation(i), stack, false) > 0)
            {
                return (INetworkPipe) tile;
            }
        }
        return null;
    }

    /** Collects info about the surrounding 6 tiles and orders them into drain-able(ITankContainer)
     * and fill-able(TileEntityPipes) instances */
    public void refresh()
    {
        // cleanup
        this.connected = HelperMethods.getSurroundingTileEntities(this);
        // read surroundings
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
        {
            TileEntity tileEntity = connected[dir.ordinal()];
            if (tileEntity instanceof ITileConnector)
            {
                if (this.canTileConnect(Connection.FLUIDS, dir.getOpposite()))
                {
                    this.connected[dir.ordinal()] = tileEntity;
                }
            }
            else if (tileEntity instanceof IFluidHandler)
            {
                this.connected[dir.ordinal()] = tileEntity;
            }
        }
    }

    @Override
    public boolean canTileConnect(Connection type, ForgeDirection dir)
    {
        return type == Connection.FLUIDS;
    }

    @Override
    public String getMeterReading(EntityPlayer user, ForgeDirection side, EnumTools tool)
    {
        // TODO maybe debug on # of connected units of input/output
        String output = "";
        if (!worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
        {
            output += "Open";
        }
        else
        {
            output += "Closed";
        }
        return output;
    }
}

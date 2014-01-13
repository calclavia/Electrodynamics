package resonantinduction.mechanical.fluid.pipe;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.fluid.IFluidNetwork;
import resonantinduction.api.fluid.IFluidPipe;
import resonantinduction.core.tilenetwork.ITileConnector;
import resonantinduction.mechanical.fluid.network.NetworkPipes;
import resonantinduction.mechanical.fluid.prefab.TileFluidNetwork;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.FluidHelper;
import dark.lib.helpers.ColorCode;
import dark.lib.helpers.ColorCode.IColorCoded;

public class TilePipe extends TileFluidNetwork implements IColorCoded, IFluidPipe
{
    /** gets the current color mark of the pipe */
    @Override
    public ColorCode getColor()
    {
        return EnumPipeType.getColorCode(this.colorID);
    }

    /** sets the current color mark of the pipe */
    @Override
    public boolean setColor(Object cc)
    {
        if (!worldObj.isRemote)
        {
            int p = this.colorID;
            this.colorID = EnumPipeType.getUpdatedID(colorID, ColorCode.get(cc));
            return p != this.colorID;
        }
        return false;
    }

    @Override
    public void validateConnectionSide(TileEntity tileEntity, ForgeDirection side)
    {
        int meta = new Vector3(this).getBlockMetadata(this.worldObj);
        if (meta < FluidContainerMaterial.values().length)
        {
            FluidContainerMaterial pipeMat = FluidContainerMaterial.values()[meta];
            if (pipeMat == FluidContainerMaterial.WOOD || pipeMat == FluidContainerMaterial.STONE)
            {
                if (side == ForgeDirection.UP)
                {
                    return;
                }
            }
        }
        if (tileEntity instanceof TilePipe)
        {
            int metaOther = new Vector3(tileEntity).getBlockMetadata(this.worldObj);
            if (meta < FluidContainerMaterial.values().length && metaOther < FluidContainerMaterial.values().length)
            {
                FluidContainerMaterial pipeMat = FluidContainerMaterial.values()[meta];
                FluidContainerMaterial pipeMatOther = FluidContainerMaterial.values()[metaOther];
                // Same pipe types can connect
                if (pipeMat == pipeMatOther)
                {
                    this.getNetwork().merge(((IFluidPipe) tileEntity).getNetwork());
                    connectedBlocks[side.ordinal()] = tileEntity;
                    setRenderSide(side, true);
                }
                else if ((pipeMat == FluidContainerMaterial.WOOD || pipeMat == FluidContainerMaterial.STONE) && (pipeMatOther == FluidContainerMaterial.WOOD || pipeMatOther == FluidContainerMaterial.STONE))
                {
                    // Wood and stone pipes can connect to each other but not other pipe types since
                    // they are more like a trough than a pipe
                    this.getNetwork().merge(((IFluidPipe) tileEntity).getNetwork());
                    connectedBlocks[side.ordinal()] = tileEntity;
                    setRenderSide(side, true);
                }
                else if (pipeMat != FluidContainerMaterial.WOOD && pipeMat != FluidContainerMaterial.STONE && pipeMatOther != FluidContainerMaterial.WOOD && pipeMatOther != FluidContainerMaterial.STONE && pipeMat != FluidContainerMaterial.GLASS && pipeMatOther != FluidContainerMaterial.GLASS)
                {
                    /*
                     * Any other pipe can connect to each other as long as the color matches except
                     * for glass which only works with itself at the moment
                     */
                    this.getNetwork().merge(((IFluidPipe) tileEntity).getNetwork());
                    connectedBlocks[side.ordinal()] = tileEntity;
                    setRenderSide(side, true);
                }
            }
        }
        else if (tileEntity instanceof IFluidHandler)
        {
            connectedBlocks[side.ordinal()] = tileEntity;
            setRenderSide(side, true);
        }
        else if (tileEntity instanceof ITileConnector && ((ITileConnector) tileEntity).canTileConnect(Connection.FLUIDS, side.getOpposite()))
        {
            connectedBlocks[side.ordinal()] = tileEntity;
            setRenderSide(side, true);
        }

    }

    @Override
    public PipeNetwork getNetwork()
    {
        if (!(this.network instanceof PipeNetwork))
        {
            this.setNetwork(new PipeNetwork(this));
        }
        return (PipeNetwork) this.network;
    }

    @Override
    public void setNetwork(IFluidNetwork network)
    {
        if (network instanceof PipeNetwork)
        {
            this.network = (PipeNetwork) network;
        }
    }

    @Override
    public void sendTankUpdate(int index)
    {
        if (this.getBlockMetadata() == FluidContainerMaterial.WOOD.ordinal() || this.getBlockMetadata() == FluidContainerMaterial.STONE.ordinal())
        {
            super.sendTankUpdate(index);
        }
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
        return false;
    }

    @Override
    public int getPressureIn(ForgeDirection side)
    {
        return this.getMaxPressure();
    }

    @Override
    public void onWrongPressure(ForgeDirection side, int pressure)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int getMaxPressure()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getMaxFlowRate()
    {
        // TODO Auto-generated method stub
        return 0;
    }
}

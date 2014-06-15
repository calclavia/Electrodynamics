package resonantinduction.mechanical.fluid.pipe;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;
import resonant.api.grid.INode;
import resonant.api.grid.INodeProvider;
import resonant.lib.grid.NodeRegistry;
import resonant.lib.utility.WorldUtility;
import resonantinduction.core.grid.fluid.FluidPressureNode;
import resonantinduction.core.grid.fluid.IPressureNodeProvider;

/** Pressure node for the pipe
 * 
 * @author Calclavia, Darkguardsman */
public class PipePressureNode extends FluidPressureNode
{
    public PipePressureNode(PartPipe parent)
    {
        super(parent);
    }

    public PartPipe pipe()
    {
        return (PartPipe) this.parent;
    }

    @Override
    public void doRecache()
    {
        connections.clear();

        if (world() != null)
        {
            byte previousConnections = pipe().getAllCurrentConnections();
            pipe().currentConnections = 0;

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
            {
                TileEntity tile = position().translate(dir).getTileEntity(world());

                if (tile instanceof IFluidHandler)
                {
                    if (tile instanceof INodeProvider)
                    {
                        // If anything happens while trying to access the node then forget about it.
                        INode check = null;
                        try
                        {
                            check = ((INodeProvider) tile).getNode(FluidPressureNode.class, dir.getOpposite());
                        }
                        catch (Exception err)
                        {
                            check = null;
                        }

                        if (check != null && check instanceof FluidPressureNode && canConnect(dir, check) && ((FluidPressureNode) check).canConnect(dir.getOpposite(), this))
                        {
                            pipe().currentConnections = WorldUtility.setEnableSide(pipe().currentConnections, dir, true);
                            connections.put(check, dir);
                        }
                    }
                    else if (canConnect(dir, tile))
                    {
                        pipe().currentConnections = WorldUtility.setEnableSide(pipe().currentConnections, dir, true);
                        connections.put(tile, dir);
                    }
                }
            }

            /** Only send packet updates if visuallyConnected changed. */
            if (!world().isRemote && previousConnections != pipe().currentConnections)
            {
                pipe().sendConnectionUpdate();
            }
        }
    }

    @Override
    public boolean canConnect(ForgeDirection from, Object source)
    {
        if (!pipe().isBlockedOnSide(from))
        {
            if (source instanceof FluidPressureNode)
            {
                FluidPressureNode otherNode = (FluidPressureNode) source;

                if (otherNode.parent instanceof PartPipe)
                {
                    PartPipe otherPipe = (PartPipe) otherNode.parent;

                    if (!otherPipe.isBlockedOnSide(from.getOpposite()) && pipe().getMaterial() == otherPipe.getMaterial())
                    {
                        return pipe().getColor() == otherPipe.getColor() || (pipe().getColor() == pipe().DEFAULT_COLOR || otherPipe.getColor() == pipe().DEFAULT_COLOR);
                    }

                    return false;
                }
            }

            return super.canConnect(from, source) || source instanceof IFluidHandler;
        }

        return false;
    }
    
    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + this.hashCode();
    }
}

package resonantinduction.mechanical.fluid.pipe;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;
import resonant.lib.utility.WorldUtility;
import resonantinduction.core.prefab.node.NodePressure;
import resonantinduction.core.prefab.part.TColorable;
import resonantinduction.core.prefab.part.TColorable$;
import universalelectricity.api.core.grid.INode;
import universalelectricity.api.core.grid.INodeProvider;

/** Pressure node for the pipe
 *
 * @author Calclavia, Darkguardsman */
public class PipePressureNode extends NodePressure
{
    public PipePressureNode(PartPipe parent)
    {
        super(parent);
    }

    public PartPipe pipe()
    {
        return (PartPipe) this.getParent();
    }


    public void doRecache()
    {
        connections.clear();

        if (world() != null)
        {
            byte previousConnections = pipe().getAllCurrentConnections();
            pipe().connectionMask_$eq((byte) 0);

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
            {
                TileEntity tile = position().add(dir).getTileEntity(world());

                if (tile instanceof IFluidHandler)
                {
                    if (tile instanceof INodeProvider)
                    {
                        // If anything happens while trying to access the node then forget about it.
                        INode check = null;
                        try
                        {
                            check = ((INodeProvider) tile).getNode(NodePressure.class, dir.getOpposite());
                        }
                        catch (Exception err)
                        {
                            check = null;
                        }

                        if (check != null && check instanceof NodePressure && canConnect(dir, check) && ((NodePressure) check).canConnect(dir.getOpposite(), this))
                        {
                            pipe().connectionMask_$eq(WorldUtility.setEnableSide(pipe().connectionMask(), dir, true));
                            connections.put(check, dir);
                        }
                    }
                    else if (canConnect(dir, tile))
                    {
                        pipe().connectionMask_$eq(WorldUtility.setEnableSide(pipe().connectionMask(), dir, true));
                        connections.put(tile, dir);
                    }
                }
            }

            /** Only send packet updates if visuallyConnected changed. */
            if (!world().isRemote && previousConnections != pipe().connectionMask())
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
            if (source instanceof NodePressure)
            {
                NodePressure otherNode = (NodePressure) source;

                if (otherNode.getParent() instanceof PartPipe)
                {
                    PartPipe otherPipe = (PartPipe) otherNode.getParent();

                    if (!otherPipe.isBlockedOnSide(from.getOpposite()) && pipe().material() == otherPipe.material())
                    {
                        return pipe().getColor() == otherPipe.getColor() || (pipe().getColor() == TColorable$.MODULE$.defaultColor() || otherPipe.getColor() == TColorable$.MODULE$.defaultColor());
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

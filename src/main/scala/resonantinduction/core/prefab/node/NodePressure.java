package resonantinduction.core.prefab.node;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import universalelectricity.api.core.grid.INodeProvider;
import universalelectricity.api.core.grid.IUpdate;

import java.util.Map;

/**
 * Created by robert on 8/15/2014.
 */
public class NodePressure extends NodeTank implements IUpdate
{
    private int pressure = 0;

    public NodePressure(INodeProvider parent)
    {
        super(parent);
    }

    public NodePressure(INodeProvider parent, int buckets)
    {
        super(parent, buckets);
    }

    @Override
    public void update(double deltaTime)
    {
        if (!world().isRemote)
        {
            updatePressure();
            if(getFluid() != null) {
                for (Map.Entry<Object, ForgeDirection> entry : connections.entrySet()) {
                    if (entry.getKey() instanceof INodeProvider && ((INodeProvider) entry.getKey()).getNode(NodePressure.class, entry.getValue().getOpposite()) instanceof NodePressure) {
                        NodePressure node = (NodePressure) ((INodeProvider) entry.getKey()).getNode(NodePressure.class, entry.getValue().getOpposite());
                        if (node.getPressure(entry.getValue().getOpposite()) <= getPressure(entry.getValue())) {

                        }
                    } else if (entry.getKey() instanceof INodeProvider && ((INodeProvider) entry.getKey()).getNode(NodeTank.class, entry.getValue().getOpposite()) instanceof NodeTank) {
                        NodeTank node = (NodeTank) ((INodeProvider) entry.getKey()).getNode(NodeTank.class, entry.getValue().getOpposite());
                        if (node.canFill(entry.getValue().getOpposite(), getFluid().getFluid()))
                        {
                            FluidStack stack = drain(Integer.MAX_VALUE, false);
                            int drained = node.fill(stack, true);
                            drain(drained, true);
                        }
                    } else if (entry.getKey() instanceof IFluidHandler)
                    {
                        if(((IFluidHandler) entry.getKey()).canFill(entry.getValue().getOpposite(), getFluid().getFluid()))
                        {
                            FluidStack stack = drain(Integer.MAX_VALUE, false);
                            int drained = ((IFluidHandler) entry.getKey()).fill(entry.getValue().getOpposite(), stack, true);
                            drain(drained, true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean canUpdate()
    {
        return true;
    }

    @Override
    public boolean continueUpdate()
    {
        return true;
    }

    protected void updatePressure()
    {
        int totalPressure = 0;
        int connectionSize = connections.size();
        int minPressure = 0;
        int maxPressure = 0;

        for(Map.Entry<Object, ForgeDirection> entry : connections.entrySet())
        {
            if(entry.getKey() instanceof INodeProvider &&  ((INodeProvider) entry.getKey()).getNode(NodePressure.class, entry.getValue().getOpposite()) instanceof NodePressure)
            {
                NodePressure node = (NodePressure) ((INodeProvider) entry.getKey()).getNode(NodePressure.class, entry.getValue().getOpposite());
                int pressure = node.getPressure(entry.getValue().getOpposite());
                minPressure = Math.min(pressure, minPressure);
                maxPressure = Math.max(pressure, maxPressure);
                totalPressure += pressure;
            }
        }

        if (connectionSize == 0)
        {
            setPressure(0);
        }
        else
        {
            if (minPressure < 0)
            {
                minPressure += 1;
            }
            if (maxPressure > 0)
            {
                maxPressure -= 1;
            }

            setPressure(Math.max(minPressure, Math.min(maxPressure, totalPressure / connectionSize + Integer.signum(totalPressure))));
        }
    }

    public int getPressure(ForgeDirection direction) {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }
}

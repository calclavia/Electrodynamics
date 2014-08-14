package resonantinduction.core.prefab;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;
import universalelectricity.api.core.grid.IConnector;
import universalelectricity.api.core.grid.INode;
import universalelectricity.api.core.grid.INodeProvider;
import universalelectricity.core.grid.Node;

import java.util.Map;

/**
 * Created by robert on 8/14/2014.
 */
public class NodeTank extends FluidTank implements INode, IConnector {

    protected INodeProvider parent = null;
    protected Map<Object, ForgeDirection> connections = null;

    public NodeTank(INodeProvider parent, int cap)
    {
        super(cap);
        this.parent = parent;
    }

    @Override
    public void reconstruct()
    {

    }

    @Override
    public void deconstruct()
    {
        connections = null;
    }

    @Override
    public void recache()
    {

    }


    @Override
    public Map<Object, ForgeDirection> getConnections(Class<? extends INode> node)
    {
        if(node.isAssignableFrom(getClass()))
        {
            return connections;
        }
        return null;
    }

    @Override
    public boolean canConnect(ForgeDirection direction, Object object)
    {
        return connections != null && connections.containsKey(object);
    }

    @Override
    public void update(double deltaTime) {

    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
    }

    @Override
    public void save(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
    }
}

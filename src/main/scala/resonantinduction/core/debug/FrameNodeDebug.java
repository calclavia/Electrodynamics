package resonantinduction.core.debug;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.api.grid.INode;
import resonant.api.grid.INodeProvider;
import codechicken.multipart.TMultiPart;

/** @author Darkguardsman */
@SuppressWarnings("serial")
public class FrameNodeDebug extends FrameDebug
{
    protected INodeProvider nodeProvider = null;
    /** Linked node */
    protected INode node = null;
    protected Class<? extends INode> nodeClazz = null;

    /** Are we debugging a node */

    public FrameNodeDebug(TileEntity tile, Class<? extends INode> nodeClazz)
    {
        super(tile);
        this.nodeClazz = nodeClazz;
    }

    public FrameNodeDebug(INodeProvider node, Class<? extends INode> nodeClazz)
    {
        super();
        this.nodeProvider = node;
        this.nodeClazz = nodeClazz;
    }

    public FrameNodeDebug(INode node)
    {
        super();
        this.node = node;
    }

    /** Gets the node used for debug */
    public INode getNode()
    {
        if (tile instanceof INodeProvider && nodeClazz != null)
        {
            return ((INodeProvider) tile).getNode(nodeClazz, ForgeDirection.UNKNOWN);
        }
        else if (nodeProvider != null && nodeClazz != null)
        {
            return nodeProvider.getNode(nodeClazz, ForgeDirection.UNKNOWN);
        }
        return node;
    }

    @Override
    public double z()
    {
        if (nodeProvider instanceof TileEntity)
        {
            return ((TileEntity) nodeProvider).zCoord;
        }
        else if (nodeProvider instanceof TMultiPart)
        {
            return ((TMultiPart) nodeProvider).z();
        }
        return super.z();
    }

    @Override
    public double x()
    {
        if (nodeProvider instanceof TileEntity)
        {
            return ((TileEntity) nodeProvider).xCoord;
        }
        else if (nodeProvider instanceof TMultiPart)
        {
            return ((TMultiPart) nodeProvider).x();
        }
        return super.x();
    }

    @Override
    public double y()
    {
        if (nodeProvider instanceof TileEntity)
        {
            return ((TileEntity) nodeProvider).yCoord;
        }
        else if (nodeProvider instanceof TMultiPart)
        {
            return ((TMultiPart) nodeProvider).y();
        }
        return super.y();
    }

    @Override
    public World world()
    {
        if (nodeProvider instanceof TileEntity)
        {
            return ((TileEntity) nodeProvider).getWorldObj();
        }
        else if (nodeProvider instanceof TMultiPart)
        {
            return ((TMultiPart) nodeProvider).world();
        }
        return super.world();
    }
}

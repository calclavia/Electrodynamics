package resonantinduction.core.prefab.tile;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.IRotatable;
import resonant.api.grid.INode;
import resonant.api.grid.INodeProvider;
import resonant.lib.content.module.TileBase;
import resonant.lib.utility.nbt.ISaveObj;

/** Prefab for tiles that support several nodes at a single time
 * 
 * @author Darkguardsman */
public class TileNode extends TileBase implements INodeProvider, IRotatable
{
    /** Set of nodes that this tile contains */
    int maxNodeId = 0;
    final HashMap<Integer, NodeWrapper> nodes = new HashMap<Integer, NodeWrapper>();

    public static class NodeWrapper
    {
        private int id;
        private INode node;
        //TODO move connectionSet to node interface
        private EnumSet<ForgeDirection> connectionSet;

        public NodeWrapper(int id, INode node)
        {
            this.id = id;
            this.node = node;
            connectionSet = EnumSet.allOf(ForgeDirection.class);
        }

        public int getID()
        {
            return id;
        }

        public INode getNode()
        {
            return node;
        }

        public boolean canConnect(ForgeDirection side)
        {
            return connectionSet.contains(side);
        }
    }

    public TileNode(Material material)
    {
        super(material);
    }

    public TileNode(String name, Material material)
    {
        super(name, material);
    }

    @Override
    public void initiate()
    {
        rebuildNodes();
        super.initiate();
    }

    public void addNode(INode node)
    {
        NodeWrapper wrapper = new NodeWrapper(maxNodeId, node);
        nodes.put(maxNodeId, wrapper);
        maxNodeId++;
    }

    /** Called to let the nodes rebuild followed by updating the connection map */
    public final void rebuildNodes()
    {
        for (NodeWrapper wrapper : nodes.values())
        {
            rebuildNode(wrapper.getNode());
        }
    }

    /** Called to rebuild the exact node */
    public void rebuildNode(INode node)
    {
        node.reconstruct();
    }

    @Override
    public INode getNode(Class<? extends INode> nodeType, ForgeDirection from)
    {
        for (NodeWrapper wrapper : nodes.values())
        {
            if (nodeType.isAssignableFrom(wrapper.getNode().getClass()))
            {
                if (wrapper.canConnect(from))
                {
                    return wrapper.getNode();
                }
            }
        }
        return null;
    }

    @Override
    public void setDirection(ForgeDirection direction)
    {
        ForgeDirection lastDir = getDirection();
        super.setDirection(direction);
        if (lastDir != getDirection())
        {
            rebuildNodes();
        }
    }

    @Override
    public void invalidate()
    {
        for (NodeWrapper wrapper : nodes.values())
        {
            wrapper.getNode().deconstruct();
        }
        super.invalidate();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        if (nbt.hasKey("NodeSaves"))
        {
            List list = nbt.getTagList("NodeSaves").tagList;
            for (int i = 0; i < list.size(); i++)
            {
                Object obj = list.get(i);
                if (obj instanceof NBTTagCompound)
                {
                    NBTTagCompound tag = (NBTTagCompound) obj;
                    int nodeID = tag.getInteger("nodeID");
                    NodeWrapper wrapper = nodes.get(nodeID);
                    if (wrapper != null && wrapper.getNode() instanceof ISaveObj)
                    {
                        ((ISaveObj) wrapper.getNode()).load(tag);
                    }
                }
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        NBTTagList list = new NBTTagList();
        for (NodeWrapper wrapper : nodes.values())
        {
            if (wrapper.getNode() instanceof ISaveObj)
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("nodeID", wrapper.id);
                ((ISaveObj) wrapper.getNode()).save(tag);
            }
        }
        nbt.setTag("NodeSaves", list);
    }

}

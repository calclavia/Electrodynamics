package resonantinduction.mechanical.gear.dev;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import codechicken.multipart.TileMultipart;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;

/** @author robert */
public class GearPathFinder
{
    /** A list of nodes that the pathfinder already went through. */
    public final Set<Vector3> closedSet = new LinkedHashSet<Vector3>();

    /** The resulted path found by the pathfinder. Could be null if no path was found. */
    public final Set<Vector3> results = new LinkedHashSet<Vector3>();

    protected final List<Object> ignoreConnector;
    protected final Vector3 target;

    public GearPathFinder(VectorWorld target, Object... ignoreConnector)
    {
        this.target = target;
        if (ignoreConnector != null)
        {
            this.ignoreConnector = Arrays.asList(ignoreConnector);
        }
        else
        {
            this.ignoreConnector = new ArrayList<Object>();
        }
    }

    public GearPathFinder(Object... ignoreConnector)
    {
        this(null, ignoreConnector);
    }

    /** A recursive function to find all connectors.
     * 
     * @return True on success finding, false on failure. */
    public boolean findNodes(VectorWorld currentNode)
    {
        this.closedSet.add(currentNode);

        //Have we found our target
        if (this.onSearch(currentNode))
        {
            return true;
        }

        //Search threw connected nodes
        for (VectorWorld node : this.getConnectedNodes(currentNode))
        {
            //If we have not pathed this node look for nodes connected to it
            if (!this.closedSet.contains(node))
            {
                //If we are done return true to stop the pathfinder
                if (this.findNodes(node))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /** Finds all the nodes connected to this node */
    public Set<VectorWorld> getConnectedNodes(VectorWorld currentNode)
    {
        Set<VectorWorld> connectedNodes = new HashSet<VectorWorld>();
        if (currentNode != null)
        {
            TileEntity currentTile = currentNode.getTileEntity();
            if (currentTile != null)
            {
                if (currentTile instanceof TileMultipart)
                {
                    
                }
                else
                {
                    for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
                    {
                        VectorWorld pos = (VectorWorld) currentNode.clone().translate(direction);
                        TileEntity tile = pos.getTileEntity();

                        if (isValid(pos) && isValid(tile))
                        {
                            connectedNodes.add(pos);
                        }
                    }
                }
            }
        }

        return connectedNodes;
    }

    /** Checks if its a valid node to path threw */
    public boolean isValid(VectorWorld node)
    {
        return node != null && !this.ignoreConnector.contains(node);
    }

    /** Checks if its a valid tile to path threw */
    public boolean isValid(TileEntity tile)
    {
        if (tile != null)
        {
            return !this.ignoreConnector.contains(tile);
        }
        return false;
    }

    /** Called each node that is searched */
    protected boolean onSearch(Vector3 node)
    {
        if (node == target)
        {
            this.results.add(node);
            return true;
        }

        return false;
    }

    public void reset()
    {
        this.results.clear();
        this.closedSet.clear();
    }

}
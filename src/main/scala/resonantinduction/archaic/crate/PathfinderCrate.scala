package resonantinduction.archaic.crate

import java.util.{ArrayList, List}

import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection
import universalelectricity.core.transform.vector.VectorWorld

/**
 * A class that allows flexible path finding in Minecraft Blocks. Back Ported from UE 1.3.0.
 * <p/>
 * TODO: Will need to change when MC 1.5 comes out.
 *
 * @author Calclavia
 */
object PathfinderCrate
{

    abstract trait IPathCallBack
    {
        /**
         * Is this a valid node to search for?
         *
         * @return
         */
        def isValidNode(finder: PathfinderCrate, direction: ForgeDirection, provider: TileEntity, node: TileEntity): Boolean

        /**
         * Called when looping through nodes.
         *
         * @param finder
         * @param provider
         * @return True to stop the path finding operation.
         */
        def onSearch(finder: PathfinderCrate, provider: TileEntity): Boolean
    }

}

class PathfinderCrate
{
    def this()
    {
        this()
        this.callBackCheck = new PathfinderCrate.IPathCallBack
        {
            def isValidNode(finder: PathfinderCrate, direction: ForgeDirection, provider: TileEntity, node: TileEntity): Boolean =
            {
                return node.isInstanceOf[TileCrate]
            }

            def onSearch(finder: PathfinderCrate, provider: TileEntity): Boolean =
            {
                return false
            }
        }
        this.clear
    }

    def findNodes(provider: TileEntity): Boolean =
    {
        if (provider != null)
        {
            this.iteratedNodes.add(provider)
            if (this.callBackCheck.onSearch(this, provider))
            {
                return false
            }
            for (i <- 0 to 6)
            {
                val vec: VectorWorld = new VectorWorld(provider)
                vec.addEquals(ForgeDirection.getOrientation(i))
                val connectedTile: TileEntity = vec.getTileEntity
                if (!iteratedNodes.contains(connectedTile))
                {
                    if (this.callBackCheck.isValidNode(this, ForgeDirection.getOrientation(i), provider, connectedTile))
                    {
                        if (!this.findNodes(connectedTile))
                        {
                            return false
                        }
                    }
                }
            }
        }
        return true
    }

    /**
     * Called to execute the pathfinding operation.
     */
    def init(provider: TileEntity): PathfinderCrate =
    {
        this.findNodes(provider)
        return this
    }

    def clear: PathfinderCrate =
    {
        this.iteratedNodes = new ArrayList[TileEntity]
        this.results = new ArrayList[_]
        return this
    }

    /**
     * A pathfinding call back interface used to call back on paths.
     */
    var callBackCheck: PathfinderCrate.IPathCallBack = null
    /**
     * A list of nodes that the pathfinder went through.
     */
    var iteratedNodes: List[TileEntity] = null
    /**
     * The results and findings found by the pathfinder.
     */
    var results: List[_] = null
}
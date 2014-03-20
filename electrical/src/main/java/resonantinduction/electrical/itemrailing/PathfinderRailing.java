package resonantinduction.electrical.itemrailing;

import calclavia.lib.path.IPathCallBack;
import calclavia.lib.path.Pathfinder;
import calclavia.lib.path.PathfinderAStar;
import universalelectricity.api.vector.Vector3;

import java.util.Set;

/**
 * @since 20/03/14
 * @author tgame14
 */
public class PathfinderRailing extends PathfinderAStar
{
    public PathfinderRailing (IPathCallBack callBack, Vector3 goal)
    {
        super(callBack, goal);
    }



    private static class PathCallBackRailing implements IPathCallBack
    {

        @Override
        public Set<Vector3> getConnectedNodes (Pathfinder finder, Vector3 currentNode)
        {
            return null;
        }

        @Override
        public boolean onSearch (Pathfinder finder, Vector3 start, Vector3 currentNode)
        {
            return false;
        }
    }


}

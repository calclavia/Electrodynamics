package resonantinduction.api.coding;

import java.util.List;

import universalelectricity.api.vector.Vector2;

/** Task in which it doesn't go right to the next task in the row. In this case the task will store
 * the entry point, and exit points. As well handle anything in between. Examples are IF statements
 * and loops. Do your logic in the refresh method as it should be called each time a new task is
 * selected.
 * 
 * @author DarkGuardsman */
public interface ILogicTask extends ITask
{
    /** There is always only one exit though you can do logic to pick from all your exit points. Exit
     * is the next task rather than the exit of the statement. Use #IRedirectTask to force the logic
     * back to this task. */
    public ITask getExitPoint();

    public List<Vector2> getExits();

    /** Mainly used by the encoder to understand the limit on connections */
    public int getMaxExitPoints();

    /** Adds a possible exit point to the split off */
    public void setExitPoint(int i, ITask task);

}

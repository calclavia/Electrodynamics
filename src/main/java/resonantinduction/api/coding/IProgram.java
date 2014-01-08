package resonantinduction.api.coding;

import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;
import universalelectricity.api.vector.Vector2;

/** Flow chart style program. Each command in the program needs to have a stored location so it can
 * be saved and loaded with its correct connections. Though the location only need to be a simple
 * Column and row based system.
 * 
 * @author DarkGuardsman */
public interface IProgram extends Cloneable
{
    /** Called when the program is added to an encoder, machine, or devices. */
    public void init(IProgrammableMachine machine);

    public IProgrammableMachine getMachine();

    /** Variables this program has to operate. Is still limited by the actual machine. String is the
     * name, Object is the starting value and data type */
    public HashMap<String, Object> getDeclairedVarables();

    /** Next task in the set. Its up to the program to increment down the list */
    public ITask getNextTask();

    /** Gets a task at the given x y location in the program */
    public ITask getTaskAt(int col, int row);

    /** Returns the entire program as a map as grid locations and tasks. */
    public HashMap<Vector2, ITask> getTaskMap();

    /** Sets the task at the point overriding what was there. If the task is null remove it and shift
     * everything up one */
    public void setTaskAt(int col, int row, ITask task);

    /** Inserts a task at the point. If a task is already there everything should shift down 1 */
    public void insertTask(int col, int row, ITask task);

    /** Return this program to its starting conditions */
    public void reset();

    /** Sets the declared variable */
    public void setVar(String name, Object object);

    /** Gets a declared variable */
    public Object getVar(String name);

    /** return size in commands high and wide */
    public Vector2 getSize();

    public NBTTagCompound save(NBTTagCompound nbt);

    public void load(NBTTagCompound nbt);
}

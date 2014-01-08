package resonantinduction.assemblyline.armbot.command;

import java.util.ArrayList;
import java.util.List;

import resonantinduction.assemblyline.api.coding.IRedirectTask;
import resonantinduction.assemblyline.api.coding.ITask;
import resonantinduction.assemblyline.armbot.TaskBase;
import net.minecraft.nbt.NBTTagCompound;
import universalelectricity.api.vector.Vector2;

/** @author DarkGuardsman */
public class TaskGOTO extends TaskBase implements IRedirectTask
{
    protected ITask task;
    protected Vector2 taskPos;
    protected boolean render = true;
    protected List<Vector2> exits = new ArrayList<Vector2>();

    public TaskGOTO()
    {
        super("GoTo", TaskType.DECISION);
    }

    public TaskGOTO(int x, int y)
    {
        this();
        this.taskPos = new Vector2(x, y);
    }

    public TaskGOTO(boolean render)
    {
        this();
        this.render = render;
    }

    @Override
    public ITask getExitPoint()
    {
        return task;
    }

    @Override
    public void refresh()
    {
        super.refresh();
        if (task == null && taskPos != null)
        {
            this.task = this.program.getTaskAt(taskPos.intX(), taskPos.intY());
        }
        this.exits.clear();
        if (this.task != null)
        {
            this.exits.add(new Vector2(this.task.getCol(), this.task.getRow()));
        }
    }

    @Override
    public void setExitPoint(int i, ITask task)
    {
        if (i == 0)
        {
            this.task = task;
        }
    }

    public void setExitPoint(int i, Vector2 vector2)
    {
        if (i == 0)
        {
            this.taskPos = vector2;
        }
    }

    @Override
    public int getMaxExitPoints()
    {
        return 1;
    }

    @Override
    public boolean render()
    {
        return this.render;
    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        super.loadProgress(nbt);
        this.taskPos = new Vector2(nbt.getDouble("entryX"), (nbt.getDouble("entryY")));
    }

    @Override
    public void save(NBTTagCompound nbt)
    {
        super.saveProgress(nbt);
        if (this.task != null)
        {
            nbt.setDouble("entryX", this.task.getCol());
            nbt.setDouble("entryY", this.task.getRow());
        }
    }

    @Override
    public TaskBase clone()
    {
        return new TaskGOTO();
    }

    @Override
    public List<Vector2> getExits()
    {
        // TODO Auto-generated method stub
        return exits;
    }

}

package com.builtbroken.assemblyline.armbot.command;

import java.util.ArrayList;
import java.util.List;

import universalelectricity.api.vector.Vector2;

import com.builtbroken.assemblyline.api.coding.IProgrammableMachine;
import com.builtbroken.assemblyline.api.coding.ITask;
import com.builtbroken.assemblyline.api.coding.args.ArgumentData;
import com.builtbroken.assemblyline.armbot.TaskBaseLogic;

/** @author DarkGuardsman */
public class TaskIF extends TaskBaseLogic
{
    protected ITask exitTruePoint = null;
    protected ITask exitFalsePoint = null;
    protected List<Vector2> exits = new ArrayList<Vector2>();
    protected boolean isTrue = false;

    public TaskIF()
    {
        super("IF");
        this.args.add(new ArgumentData("check", "statement"));
        this.args.add(new ArgumentData("compare", "statement"));
        this.UV = new Vector2(0, 120);
    }

    public TaskIF(ITask trueExit, ITask falseExit)
    {
        this();
        this.exitTruePoint = trueExit;
        this.exitFalsePoint = falseExit;

    }

    @Override
    public void refresh()
    {
        super.refresh();
        if (this.getArg("check") != null && this.getArg("compare") != null)
        {
            this.isTrue = this.getArg("check").equals(this.getArg("compare"));
        }
        if (exitTruePoint == null)
        {
            exitTruePoint = this.program.getTaskAt(this.getCol() + 1, this.getRow());
        }
        if (exitFalsePoint == null)
        {
            exitFalsePoint = this.program.getTaskAt(this.getCol(), this.getRow() + 1);
            ;
        }
    }

    @Override
    public TaskIF clone()
    {
        return new TaskIF();
    }

    @Override
    public ITask getExitPoint()
    {
        if (this.isTrue)
        {
            return this.exitTruePoint;
        }
        return this.exitFalsePoint;
    }

    @Override
    public int getMaxExitPoints()
    {
        return 2;
    }

    @Override
    public void setExitPoint(int i, ITask task)
    {
        if (i == 0)
        {
            this.exitFalsePoint = task;
        }
        else if (i == 1)
        {
            this.exitTruePoint = task;
        }
    }

    @Override
    public boolean canUseTask(IProgrammableMachine device)
    {
        return true;
    }

    @Override
    public List<Vector2> getExits()
    {
        return this.exits;
    }

}

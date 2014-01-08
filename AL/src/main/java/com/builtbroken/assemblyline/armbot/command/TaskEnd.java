package com.builtbroken.assemblyline.armbot.command;

import com.builtbroken.assemblyline.armbot.TaskBase;

/** @author DarkGuardsman */
public class TaskEnd extends TaskBase
{
    public TaskEnd()
    {
        super("end", TaskType.END);
    }

    @Override
    public TaskBase clone()
    {
        return new TaskEnd();
    }
}

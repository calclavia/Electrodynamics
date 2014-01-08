package com.builtbroken.assemblyline.armbot.command;

import com.builtbroken.assemblyline.armbot.TaskBaseProcess;

public class TaskReturn extends TaskRotateTo
{
    public TaskReturn()
    {
        super("Return", 0, 0);
    }

    @Override
    public TaskBaseProcess clone()
    {
        return new TaskReturn();
    }

}

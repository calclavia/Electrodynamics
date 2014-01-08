package com.builtbroken.assemblyline.armbot.command;

import universalelectricity.api.vector.Vector2;

import com.builtbroken.assemblyline.api.IArmbot;
import com.builtbroken.assemblyline.armbot.TaskBaseArmbot;
import com.builtbroken.assemblyline.armbot.TaskBaseProcess;

public class TaskDrop extends TaskBaseArmbot
{
    public TaskDrop()
    {
        super("drop");
        this.UV = new Vector2(20, 80);
    }

    @Override
    public ProcessReturn onUpdate()
    {
        if (super.onUpdate() == ProcessReturn.CONTINUE)
        {
            ((IArmbot) this.program.getMachine()).dropHeldObject();
        }
        return ProcessReturn.DONE;
    }

    @Override
    public TaskBaseProcess clone()
    {
        return new TaskDrop();
    }

}

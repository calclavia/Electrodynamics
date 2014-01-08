package resonantinduction.assemblyline.armbot.command;

import resonantinduction.assemblyline.armbot.TaskBase;

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

package resonantinduction.armbot.command;

import resonantinduction.armbot.TaskBase;

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

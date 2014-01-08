package resonantinduction.armbot.command;

import resonantinduction.armbot.TaskBaseProcess;

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

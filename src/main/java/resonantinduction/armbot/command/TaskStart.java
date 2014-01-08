package resonantinduction.armbot.command;

import resonantinduction.armbot.TaskBase;

/** Fake task as the player can not create, edit, or do anything with this task. Its only used to
 * allow the gui to render the task as an actual task instance
 * 
 * @author DarkGaurdsman */
public class TaskStart extends TaskBase
{
    public TaskStart()
    {
        super("Start", TaskType.START);
    }

    @Override
    public TaskBase clone()
    {
        return new TaskStart();
    }

}

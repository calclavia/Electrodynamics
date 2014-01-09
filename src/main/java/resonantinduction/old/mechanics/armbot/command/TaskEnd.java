package resonantinduction.old.mechanics.armbot.command;

import resonantinduction.old.mechanics.armbot.TaskBase;

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

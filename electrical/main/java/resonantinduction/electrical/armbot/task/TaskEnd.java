package resonantinduction.electrical.armbot.task;

import resonantinduction.electrical.armbot.TaskBase;

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

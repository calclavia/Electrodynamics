package resonantinduction.old.mechanics.armbot;

import resonantinduction.old.api.coding.ILogicTask;

/** @author DarkGuardsman */
public abstract class TaskBaseLogic extends TaskBase implements ILogicTask
{
	public TaskBaseLogic(String name)
	{
		super(name, TaskType.DECISION);
	}
}

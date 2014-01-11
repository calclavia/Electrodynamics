package resonantinduction.electrical.armbot;

import resonantinduction.electrical.encoder.coding.ILogicTask;

/** @author DarkGuardsman */
public abstract class TaskBaseLogic extends TaskBase implements ILogicTask
{
	public TaskBaseLogic(String name)
	{
		super(name, TaskType.DECISION);
	}
}

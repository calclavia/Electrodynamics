package resonantinduction.assemblyline.armbot;

import resonantinduction.assemblyline.api.coding.ILogicTask;

/** @author DarkGuardsman */
public abstract class TaskBaseLogic extends TaskBase implements ILogicTask
{
    public TaskBaseLogic(String name)
    {
        super(name, TaskType.DECISION);
    }
}

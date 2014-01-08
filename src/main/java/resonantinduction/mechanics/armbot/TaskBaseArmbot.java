package resonantinduction.mechanics.armbot;

import resonantinduction.api.IArmbot;
import resonantinduction.api.coding.IProgrammableMachine;

public abstract class TaskBaseArmbot extends TaskBaseProcess
{

	public TaskBaseArmbot(String name)
	{
		super(name);
	}

	@Override
	public ProcessReturn onMethodCalled()
	{
		if (super.onMethodCalled() == ProcessReturn.CONTINUE && this.program.getMachine() instanceof IArmbot)
		{
			return ProcessReturn.CONTINUE;
		}
		return ProcessReturn.GENERAL_ERROR;
	}

	@Override
	public ProcessReturn onUpdate()
	{
		if (super.onUpdate() == ProcessReturn.CONTINUE && this.program.getMachine() instanceof IArmbot)
		{
			return ProcessReturn.CONTINUE;
		}
		return ProcessReturn.GENERAL_ERROR;
	}

	@Override
	public boolean canUseTask(IProgrammableMachine device)
	{
		return device instanceof IArmbot;
	}

}

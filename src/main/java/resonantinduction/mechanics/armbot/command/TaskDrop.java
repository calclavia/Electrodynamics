package resonantinduction.mechanics.armbot.command;

import resonantinduction.api.IArmbot;
import resonantinduction.mechanics.armbot.TaskBaseArmbot;
import resonantinduction.mechanics.armbot.TaskBaseProcess;
import universalelectricity.api.vector.Vector2;

public class TaskDrop extends TaskBaseArmbot
{
	public TaskDrop()
	{
		super("drop");
		this.UV = new Vector2(20, 80);
	}

	@Override
	public ProcessReturn onUpdate()
	{
		if (super.onUpdate() == ProcessReturn.CONTINUE)
		{
			((IArmbot) this.program.getMachine()).dropHeldObject();
		}
		return ProcessReturn.DONE;
	}

	@Override
	public TaskBaseProcess clone()
	{
		return new TaskDrop();
	}

}

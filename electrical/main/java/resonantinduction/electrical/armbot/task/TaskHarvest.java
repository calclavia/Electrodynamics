package resonantinduction.electrical.armbot.task;

/**
 * Used by arms to break a specific block in a position.
 * 
 * @author DarkGuardsman
 */
public class TaskHarvest extends TaskBreak
{

	public TaskHarvest()
	{
		super("Harvest");
	}

	@Override
	public ProcessReturn onMethodCalled()
	{
		this.keep = true;
		return super.onMethodCalled();
	}
}

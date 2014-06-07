package mffs;

public abstract class DelayedEvent
{
	public int ticks = 0;
	protected IDelayedEventHandler handler;

	public DelayedEvent(IDelayedEventHandler handler, int ticks)
	{
		this.handler = handler;
		this.ticks = ticks;
	}

	protected abstract void onEvent();

	public void update()
	{
		this.ticks--;

		if (this.ticks <= 0)
		{
			this.onEvent();
		}
	}

	/**
	 * The higher the number, the higher the priority.
	 * 
	 * @return
	 */
	public int getPriority()
	{
		return 0;
	}
}

package mffs;

public abstract class DelayedEvent
{
	public int ticks = 0;

	public DelayedEvent(int ticks)
	{
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
}

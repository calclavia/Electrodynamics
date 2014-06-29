package mffs.mobilize.event;

public interface IDelayedEventHandler
{
	public void queueEvent(DelayedEvent evt);
}

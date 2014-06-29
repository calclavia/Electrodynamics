package mffs.event;

public interface IDelayedEventHandler
{
	public void queueEvent(DelayedEvent evt);
}

package mffs.field.mobilize.event;

public interface IDelayedEventHandler
{
	public void queueEvent(DelayedEvent evt);
}

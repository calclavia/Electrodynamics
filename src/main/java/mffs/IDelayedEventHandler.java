package mffs;

import java.util.List;

public interface IDelayedEventHandler
{
	public List<DelayedEvent> getDelayedEvents();

	public List<DelayedEvent> getQuedDelayedEvents();
}

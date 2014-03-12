package resonantinduction.core.grid;

import net.minecraftforge.event.Event;

//NO-OP
abstract class NodeProviderEvent extends Event
{
	public final INodeProvider provider;

	protected NodeProviderEvent(INodeProvider provider)
	{
		this.provider = provider;
	}

	public static class NodeProviderLoadEvent extends NodeProviderEvent
	{
		protected NodeProviderLoadEvent(INodeProvider provider)
		{
			super(provider);
		}
	}

	public static class NodeProviderUnloadEvent extends NodeProviderEvent
	{
		protected NodeProviderUnloadEvent(INodeProvider provider)
		{
			super(provider);
		}
	}
}

package resonantinduction.core.grid;

import resonantinduction.mechanical.energy.network.MechanicalNode;
import universalelectricity.api.net.IUpdate;
import universalelectricity.core.net.NetworkTickHandler;

public class TickingGrid<N extends Node> extends NodeGrid<N> implements IUpdate
{
	public TickingGrid(N node, Class type)
	{
		super(type);
		add(node);
		UpdateTicker.addNetwork(this);
	}

	/**
	 * An grid update called only server side.
	 * TODO: Make actual ticker an independent thread.
	 */
	@Override
	public void update()
	{
		//synchronized (nodes)
		{
			for (Node node : nodes)
			{
				node.update(1 / 20f);
			}
		}
	}

	@Override
	public boolean canUpdate()
	{
		return nodes.size() > 0;
	}

	@Override
	public boolean continueUpdate()
	{
		return canUpdate();
	}
}

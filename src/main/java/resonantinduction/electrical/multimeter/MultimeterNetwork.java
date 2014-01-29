package resonantinduction.electrical.multimeter;

import java.util.List;

import universalelectricity.api.net.IUpdate;
import universalelectricity.api.vector.Vector3;
import universalelectricity.core.net.Network;
import universalelectricity.core.net.NetworkTickHandler;

public class MultimeterNetwork extends Network<MultimeterNetwork, PartMultimeter> implements IUpdate
{
	public List<String> displayInformation;
	public Graph graph = new Graph(20 * 10);

	/**
	 * The absolute center of the multimeter screens.
	 */
	public Vector3 center = new Vector3();

	/**
	 * The relative bound sizes.
	 */
	private Vector3 upperBound = new Vector3();
	private Vector3 lowerBound = new Vector3();

	/**
	 * The overall size of the multimeter
	 */
	public Vector3 size = new Vector3();

	private long queueGraphValue = 0;

	@Override
	public void reconstruct()
	{
		upperBound = null;
		lowerBound = null;
		super.reconstruct();
		center = upperBound.midPoint(lowerBound);
		upperBound.subtract(center);
		lowerBound.subtract(center);
		size = new Vector3(Math.abs(upperBound.x) + Math.abs(lowerBound.x), Math.abs(upperBound.y) + Math.abs(lowerBound.y), Math.abs(upperBound.z) + Math.abs(lowerBound.z));
		NetworkTickHandler.addNetwork(this);
	}

	@Override
	public void update()
	{
		if (queueGraphValue > 0)
		{
			graph.add(queueGraphValue);
			queueGraphValue = 0;
		}
	}

	@Override
	public boolean canUpdate()
	{
		return getConnectors().size() > 0;
	}

	@Override
	public boolean continueUpdate()
	{
		return canUpdate();
	}

	public void updateGraph(long detectedValue)
	{
		queueGraphValue += detectedValue;
	}

	@Override
	public boolean isValidConnector(PartMultimeter node)
	{
		return node.world() != null && node.tile() != null;
	}

	@Override
	protected void reconstructConnector(PartMultimeter node)
	{
		node.setNetwork(this);

		if (upperBound == null)
		{
			upperBound = node.getPosition().translate(0.5);
		}

		if (lowerBound == null)
		{
			lowerBound = node.getPosition().translate(0.5);
		}

		upperBound = upperBound.max(node.getPosition().translate(0.5));
		lowerBound = lowerBound.min(node.getPosition().translate(0.5));
	}

	@Override
	public MultimeterNetwork newInstance()
	{
		return new MultimeterNetwork();
	}

}

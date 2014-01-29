package resonantinduction.electrical.multimeter;

import universalelectricity.api.vector.Vector2;
import universalelectricity.core.net.Network;

public class MultimeterNetwork extends Network<MultimeterNetwork, PartMultimeter>
{
	/**
	 * The absolute center of the multimeter screens.
	 */
	public Vector2 center = new Vector2();

	/**
	 * The relative bound sizes.
	 */
	public Vector2 upperBound = new Vector2();
	public Vector2 lowerBound = new Vector2();

	@Override
	public void reconstruct()
	{
		super.reconstruct();
		center = upperBound.midPoint(lowerBound);
		upperBound.subtract(center);
		lowerBound.subtract(center);
	}

	@Override
	protected void reconstructConnector(PartMultimeter node)
	{
		node.setNetwork(this);

		/**
		 * Computer upper bound
		 */
		if (node.getPosition().y > upperBound.x)
		{
			upperBound.x = node.getPosition().y;
		}

		if (node.getDirection().offsetX == 0)
		{
			if (node.getPosition().x > upperBound.y)
			{
				upperBound.y = node.getPosition().x;
			}
		}

		if (node.getDirection().offsetZ == 0)
		{
			if (node.getPosition().z > upperBound.y)
			{
				upperBound.y = node.getPosition().z;
			}
		}

		/**
		 * Computer lower bound
		 */
		if (node.getPosition().y < lowerBound.x)
		{
			lowerBound.x = node.getPosition().y;
		}

		if (node.getDirection().offsetX == 0)
		{
			if (node.getPosition().x < lowerBound.y)
			{
				lowerBound.y = node.getPosition().x;
			}
		}

		if (node.getDirection().offsetZ == 0)
		{
			if (node.getPosition().z < lowerBound.y)
			{
				lowerBound.y = node.getPosition().z;
			}
		}
	}

	@Override
	public MultimeterNetwork newInstance()
	{
		return new MultimeterNetwork();
	}
}

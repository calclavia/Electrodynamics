package resonantinduction.electrical.multimeter;

import net.minecraft.nbt.NBTTagCompound;
import universalelectricity.api.vector.Vector3;
import universalelectricity.core.net.Network;

public class MultimeterNetwork extends Network<MultimeterNetwork, PartMultimeter>
{
	/**
	 * The absolute center of the multimeter screens.
	 */
	public Vector3 center = new Vector3();

	/**
	 * The relative bound sizes.
	 */
	public Vector3 upperBound = new Vector3();
	public Vector3 lowerBound = new Vector3();

	@Override
	public void reconstruct()
	{
		upperBound = null;
		lowerBound = null;
		super.reconstruct();
		center = upperBound.midPoint(lowerBound);
		upperBound.subtract(center);
		lowerBound.subtract(center);
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

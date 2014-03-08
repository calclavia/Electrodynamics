package resonantinduction.mechanical.energy.grid;

import resonantinduction.core.grid.Grid;
import resonantinduction.core.grid.INodeProvider;
import resonantinduction.core.grid.Node;

public abstract class EnergyNode<P extends INodeProvider, G extends Grid, N> extends Node<P, G, N>
{
	public EnergyNode(P parent)
	{
		super(parent);
	}

	/**
	 * @return Gets the power of this node. Note that power by definition is energy per second.
	 */
	public abstract double getPower();

	/**
	 * @return Gets the energy buffered in this node at this instance.
	 */
	public abstract double getEnergy();
}

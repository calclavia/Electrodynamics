package resonantinduction.mechanical.energy.network;

import resonantinduction.core.grid.Node;

public abstract class EnergyNode extends Node
{
	/**
	 * @return Gets the power of this node. Note that power by definition is energy per second.
	 */
	public abstract double getPower();

	/**
	 * @return Gets the energy buffered in this node at this instance.
	 */
	public abstract double getEnergy();
}

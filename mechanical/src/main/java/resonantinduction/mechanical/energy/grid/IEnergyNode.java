package resonantinduction.mechanical.energy.grid;

public interface IEnergyNode
{
	/**
	 * @return Gets the power of this node. Note that power by definition is energy per second.
	 */
	public double getPower();

	/**
	 * @return Gets the energy buffered in this node at this instance.
	 */
	public double getEnergy();
}

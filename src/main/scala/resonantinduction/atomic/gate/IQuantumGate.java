package resonantinduction.atomic.gate;

import resonant.api.blocks.IBlockFrequency;

/**
 * Only TileEntities should implement this.
 *
 * @author Calclavia
 */
public interface IQuantumGate extends IBlockFrequency
{
	public void transport(Object object);
}

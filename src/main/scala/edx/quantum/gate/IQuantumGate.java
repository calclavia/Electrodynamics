package edx.quantum.gate;

import resonant.api.tile.IBlockFrequency;

/**
 * Only TileEntities should implement this.
 *
 * @author Calclavia
 */
public interface IQuantumGate extends IBlockFrequency
{
	public void transport(Object object);
}

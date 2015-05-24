package com.calclavia.edx.quantum.gate;

import resonantengine.api.tile.IBlockFrequency;

/**
 * Only TileEntities should implement this.
 *
 * @author Calclavia
 */
public interface IQuantumGate extends IBlockFrequency
{
	public void transport(Object object);
}

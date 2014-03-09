package resonantinduction.quantum.gate;

import net.minecraft.entity.Entity;
import icbm.api.IBlockFrequency;

/**
 * Only TileEntities should implement this.
 * 
 * @author Calclavia
 * 
 */
public interface IQuantumGate extends IBlockFrequency
{
	public void transport(Entity entity);
}

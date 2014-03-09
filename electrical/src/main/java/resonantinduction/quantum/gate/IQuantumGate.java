package resonantinduction.quantum.gate;

import icbm.api.IBlockFrequency;
import net.minecraft.entity.Entity;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;

/**
 * Only TileEntities should implement this.
 * 
 * @author Calclavia
 * 
 */
public interface IQuantumGate extends IBlockFrequency, IFluidHandler
{
	public void transport(Entity entity);

	FluidTank getQuantumTank();
}

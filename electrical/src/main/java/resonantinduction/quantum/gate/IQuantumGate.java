package resonantinduction.quantum.gate;

import calclavia.api.icbm.IBlockFrequency;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;

/**
 * Only TileEntities should implement this.
 * 
 * @author Calclavia
 * 
 */
public interface IQuantumGate extends IBlockFrequency, IFluidHandler, ISidedInventory
{
	public void transport(Entity entity);

	FluidTank getQuantumTank();
}

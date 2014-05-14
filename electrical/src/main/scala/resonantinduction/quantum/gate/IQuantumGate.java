package resonantinduction.quantum.gate;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;
import resonant.api.blocks.IBlockFrequency;

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

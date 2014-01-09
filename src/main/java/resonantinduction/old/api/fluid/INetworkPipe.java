package resonantinduction.old.api.fluid;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

/**
 * A machine that acts as one with the liquid network using the networks pressure for some function
 * that doesn't change the over all network pressure. So pipes, gauges, tubes, buffers, decor
 * blocks.
 */
public interface INetworkPipe extends INetworkFluidPart
{
	/**
	 * Gets the parts max pressure limit it can handle
	 * 
	 * Note this is not recommended max limit by rather actual breaking point of the part
	 */
	public double getMaxPressure(ForgeDirection side);

	/**
	 * Max flow rate of liquid flow this part from the side for the liquid type that his part will
	 * allow
	 * 
	 * @return limit in bucket parts(1/1000 of a bucket)
	 */
	public int getMaxFlowRate(FluidStack stack, ForgeDirection side);

	/**
	 * Called when the pressure on the machine goes beyond max limits. Suggest doing random chance
	 * of damage or break too simulate real chances of pipe going beyond designed limits
	 * 
	 * @param damageAllowed - can this tileEntity cause grief damage
	 * @return true if the device over pressured and destroyed itself
	 */
	public boolean onOverPressure(Boolean damageAllowed);

}

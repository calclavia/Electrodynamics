package resonantinduction.api.fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.core.tilenetwork.INetworkPart;

/**
 * Interface used by part that are members of a fluid tile network. Parts in the network will act as
 * one entity and will be controlled by the network. This means the network need the part to access
 * the parts in a set way to function correctly
 * 
 * @author DarkGuardsman
 */
public interface INetworkFluidPart extends IFluidHandler, INetworkPart
{

	/** Gets information about the tanks internal storage that the network has access to. */
	public FluidTankInfo[] getTankInfo();

	/**
	 * Fills the pipe in the same way that fill method is called in IFluidHandler. This is used so
	 * the network has a direct method to access the pipes internal fluid storage
	 */
	public int fillTankContent(int index, FluidStack stack, boolean doFill);

	/**
	 * Removes from from the pipe in the same way that drain method is called in IFluidHandler. This
	 * is used so the network has a direct method to access the pipes internal fluid storage
	 */
	public FluidStack drainTankContent(int index, int volume, boolean doDrain);
}

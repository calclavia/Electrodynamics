package dark.api.fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;
import dark.api.parts.INetworkPart;
import dark.core.interfaces.ColorCode.IColorCoded;

public interface INetworkFluidPart extends IColorCoded, IFluidHandler, INetworkPart
{
    /** Gets the part's main tank for shared storage */
    public IFluidTank getTank();

    /** Sets the content of the part's main tank */
    public void setTankContent(FluidStack stack);
}

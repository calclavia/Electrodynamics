package resonantinduction.api.mechanical.fluid;

import net.minecraftforge.common.ForgeDirection;

/**
 * Applied to tiles that are a source of pressure in a fluid network
 * 
 * @author Darkguardsman
 */
@Deprecated
public interface IPressureOutput
{
	public int getPressureOut(ForgeDirection side);
}

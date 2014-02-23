package resonantinduction.api.mechanical.fluid;

import net.minecraftforge.common.ForgeDirection;

/**
 * Applied to tiles that work with pressure for there inputs
 * 
 * @author DarkGaurdsman
 */
public interface IPressureInput extends IPressure
{
	public int getPressureIn(ForgeDirection side);

	public void onWrongPressure(ForgeDirection side, int pressure);
}

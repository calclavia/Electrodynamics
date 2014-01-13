package resonantinduction.api.fluid;

import net.minecraftforge.common.ForgeDirection;

/** Applied to tiles that work with pressure for there inputs
 * 
 * @author DarkGaurdsman */
public interface IPressureInput
{
    public int getPressureIn(ForgeDirection side);

    public void onWrongPressure(ForgeDirection side, int pressure);
}

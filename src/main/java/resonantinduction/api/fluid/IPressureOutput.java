package resonantinduction.api.fluid;

import net.minecraftforge.common.ForgeDirection;

/** Applied to tiles that are a source of pressure in a fluid network
 * 
 * @author Darkguardsman */
public interface IPressureOutput
{
    public int getPressureOut(ForgeDirection side);
}

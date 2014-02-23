package resonantinduction.api.mechanical.fluid;

import net.minecraftforge.common.ForgeDirection;

public interface IPressure
{
	public void setPressure(int amount);

	public int getPressure(ForgeDirection dir);
}

package resonantinduction.core.debug;

import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.prefab.tile.TileEnergyMachine;

public class TileEntityInfLoad extends TileEnergyMachine
{

	@Override
	public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
	{
		return receive;
	}

	@Override
	public long onExtractEnergy(ForgeDirection from, long request, boolean doExtract)
	{
		return 0;
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return true;
	}

	@Override
	public long getMaxEnergyStored()
	{
		return Long.MAX_VALUE;
	}
}

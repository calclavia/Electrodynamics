package resonantinduction.core.debug;

import net.minecraftforge.common.ForgeDirection;
import calclavia.lib.prefab.tile.TileElectrical;

public class TileInfiniteEnergySink extends TileElectrical
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
}

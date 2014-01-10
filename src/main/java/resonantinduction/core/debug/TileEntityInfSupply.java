package resonantinduction.core.debug;

import java.util.EnumSet;

import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.prefab.tile.TileEntityEnergyMachine;

public class TileEntityInfSupply extends TileEntityEnergyMachine
{
	@Override
	public void updateEntity()
	{
		super.updateEntity();
		this.setJoulesPerTick(10000);
		this.produce();
	}

	@Override
	public EnumSet<ForgeDirection> getOutputDirections()
	{
		return EnumSet.allOf(ForgeDirection.class);
	}

	@Override
	public EnumSet<ForgeDirection> getInputDirections()
	{
		return EnumSet.noneOf(ForgeDirection.class);
	}

	@Override
	public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
	{
		return 0;
	}

	@Override
	public long onExtractEnergy(ForgeDirection from, long request, boolean doExtract)
	{
		return request;
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

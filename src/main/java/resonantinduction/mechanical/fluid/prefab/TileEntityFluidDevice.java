package resonantinduction.mechanical.fluid.prefab;

import java.util.Random;

import resonantinduction.core.tilenetwork.prefab.NetworkTileEntities;
import calclavia.lib.prefab.tile.TileAdvanced;

public abstract class TileEntityFluidDevice extends TileAdvanced
{
	public Random random = new Random();

	@Override
	public void invalidate()
	{
		super.invalidate();
		NetworkTileEntities.invalidate(this);
	}
}

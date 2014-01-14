package resonantinduction.mechanical.fluid.prefab;

import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.api.IReadOut;
import resonantinduction.core.tilenetwork.ITileConnector;
import resonantinduction.core.tilenetwork.prefab.NetworkTileEntities;
import calclavia.lib.prefab.tile.TileAdvanced;

public abstract class TileEntityFluidDevice extends TileAdvanced implements ITileConnector
{
	public Random random = new Random();

	@Override
	public void invalidate()
	{
		super.invalidate();
		NetworkTileEntities.invalidate(this);
	}
}

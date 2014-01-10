package resonantinduction.old.transport.fluid.prefab;

import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.tilenetwork.ITileConnector;
import resonantinduction.core.tilenetwork.prefab.NetworkTileEntities;
import calclavia.lib.prefab.tile.TileAdvanced;
import dark.lib.interfaces.IReadOut;

public abstract class TileEntityFluidDevice extends TileAdvanced implements IReadOut, ITileConnector
{
	public Random random = new Random();

	@Override
	public void invalidate()
	{
		super.invalidate();
		NetworkTileEntities.invalidate(this);
	}

	@Override
	public String getMeterReading(EntityPlayer user, ForgeDirection side, EnumTools tool)
	{
		if (tool != null && tool == EnumTools.PIPE_GUAGE)
		{
			return " IndirectlyPower:" + this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
		}
		return null;
	}
}

package resonantinduction.mechanical.logistic.rail;

import net.minecraft.tileentity.TileEntity;

public class RedstoneControl
{
	private TileEntity tile;

	/**
	 * Settings
	 */
	private byte input;
	
	private byte output;

	public RedstoneControl(TileEntity tile)
	{
		this.tile = tile;
	}

	public boolean isActive()
	{
		return tile.worldObj.isBlockIndirectlyGettingPowered(tile.xCoord, tile.yCoord, tile.zCoord);
	}
}

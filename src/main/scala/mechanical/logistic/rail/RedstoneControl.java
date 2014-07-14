package mechanical.logistic.rail;

import net.minecraft.tileentity.TileEntity;

public class RedstoneControl
{
	private TileEntity tile;

	/**
	 * Settings
	 * 0 - Ignore Redstone
	 * 1 - Redstone Pulse
	 * 2 - Redstone Constant
	 */
	private byte input;
	private byte output;

	boolean pulse = false;
	boolean isOutputting = false;

	public RedstoneControl(TileEntity tile)
	{
		this.tile = tile;
	}

	public void update()
	{
		if (pulse)
			pulse = false;

		if (isRedstonePowered())
			pulse = true;

		if (isOutputting && output == 1)
			isOutputting = false;
	}

	public void setOutput(boolean isOutputting)
	{
		if (output > 0)
			this.isOutputting = isOutputting;
	}

	public boolean isRedstonePowered()
	{
		return tile.worldObj.isBlockIndirectlyGettingPowered(tile.xCoord, tile.yCoord, tile.zCoord);
	}

	public boolean isActive()
	{
		return pulse;
	}

	public int getOutput()
	{
		return isOutputting ? 15 : 0;
	}
}

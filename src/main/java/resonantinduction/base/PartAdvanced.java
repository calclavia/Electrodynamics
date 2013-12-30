package resonantinduction.base;

import codechicken.multipart.TMultiPart;

public abstract class PartAdvanced extends TMultiPart
{
	protected long ticks = 0;

	@Override
	public void update()
	{
		if (ticks == 0)
		{
			initiate();
		}

		if (ticks >= Long.MAX_VALUE)
		{
			ticks = 1;
		}

		ticks++;
	}

	/**
	 * Called on the TileEntity's first tick.
	 */
	public void initiate()
	{
	}
}

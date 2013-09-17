package resonantinduction.wire.multipart;

import codechicken.multipart.TMultiPart;

public abstract class PartAdvanced extends TMultiPart
{	
	protected long ticks = 0;

	@Override
	public void update()
	{
		if (this.ticks == 0)
		{
			this.initiate();
		}

		if (this.ticks >= Long.MAX_VALUE)
		{
			this.ticks = 1;
		}

		this.ticks++;
	}

	/**
	 * Called on the TileEntity's first tick.
	 */
	public void initiate()
	{
	}
}

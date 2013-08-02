/**
 * 
 */
package resonantinduction.base;

import net.minecraft.tileentity.TileEntity;

/**
 * @author Calclavia
 * 
 */
public class TileEntityBase extends TileEntity
{
	protected long ticks = 0;

	public void initiate()
	{

	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (this.ticks++ == 0)
		{
			this.initiate();
		}

	}
}

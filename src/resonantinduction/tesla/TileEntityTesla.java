/**
 * 
 */
package resonantinduction.tesla;

import resonantinduction.ITesla;
import resonantinduction.TeslaGrid;
import resonantinduction.base.TileEntityBase;

/**
 * @author Calclavia
 * 
 */
public class TileEntityTesla extends TileEntityBase implements ITesla
{
	@Override
	public void initiate()
	{
		TeslaGrid.getInstance().register(this);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

	}

	public int getRange()
	{
		return 5;
	}

	@Override
	public void invalidate()
	{
		TeslaGrid.getInstance().unregister(this);
		super.invalidate();
	}
}

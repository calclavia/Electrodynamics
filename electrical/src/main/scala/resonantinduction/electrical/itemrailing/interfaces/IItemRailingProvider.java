package resonantinduction.electrical.itemrailing.interfaces;

import resonant.api.grid.INodeProvider;
import universalelectricity.api.vector.IVectorWorld;

/**
 * @author tgame14
 * @since 20/04/14
 */
public interface IItemRailingProvider extends INodeProvider
{
	public IVectorWorld getVectorWorld();

    public void onInventoryChanged();
}

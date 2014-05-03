package resonantinduction.electrical.itemrailing.interfaces;

import calclavia.lib.grid.INodeProvider;
import universalelectricity.api.vector.IVectorWorld;
import universalelectricity.api.vector.VectorWorld;

/**
 * @author tgame14
 * @since 20/04/14
 */
public interface IItemRailingProvider extends INodeProvider
{
	public IVectorWorld getWorldPos();
}

package resonantinduction.electrical.itemrailing;

import calclavia.lib.grid.NodeGrid;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailing;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailingTransfer;

import java.util.Comparator;

/**
 * @since 18/03/14
 * @author tgame14
 */
public class GridRailing extends NodeGrid<NodeRailing>
{
    public GridRailing (Class type)
    {
        super(type);
    }

	public IItemRailing findTargetForIItemTransfer(IItemRailingTransfer itemRailingTransfer)
	{

	}

	public static class ComparatorRailing implements Comparator<IItemRailing>
	{

		@Override
		public int compare(IItemRailing o1, IItemRailing o2)
		{
			return 0;
		}
	}
}

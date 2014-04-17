package resonantinduction.electrical.itemrailing;

import calclavia.lib.grid.NodeGrid;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailing;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailingTransfer;

import java.util.Arrays;

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

	public IItemRailing findTargetForIItemTransfer(IItemRailingTransfer itemwrapper)
	{
		if (itemwrapper.getColor() == null)
		{
			return findNearestInventory(itemwrapper);
		}
		return findNearestColoredTarget(itemwrapper);
	}

	public IItemRailing findNearestInventory(IItemRailingTransfer itemwrapper)
	{
		IItemRailing[] arr = (IItemRailing[]) this.getNodes().toArray();
		Arrays.sort(arr, new RailingDistanceComparator.RailingInventoryDistanceComparator(itemwrapper.getRailing()));
		return arr[0];
	}

	public IItemRailing findNearestColoredTarget(IItemRailingTransfer itemwrapper)
	{
		IItemRailing[] arr = (IItemRailing[]) this.getNodes().toArray();
		Arrays.sort(arr, new RailingDistanceComparator.RailingColoredDistanceComparator(itemwrapper.getRailing(), itemwrapper.getColor()));
		return arr[0];
	}
}

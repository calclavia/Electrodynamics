package resonantinduction.electrical.itemrailing;

import calclavia.lib.grid.NodeGrid;
import com.google.common.collect.Sets;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailing;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailingTransfer;

import java.util.*;

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
		IItemRailing endGoal = null;
		IItemRailing[] arr = (IItemRailing[]) this.getNodes().toArray();
		Arrays.sort();

		for (NodeRailing node : this.getNodes())
		{

		}
	}

	public IItemRailing findNearestColoredTarget(IItemRailingTransfer itemwrapper)
	{

	}
}

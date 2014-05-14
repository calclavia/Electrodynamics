package resonantinduction.electrical.itemrailing;

import java.util.Arrays;

import net.minecraft.item.ItemStack;
import resonant.lib.grid.TickingGrid;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailing;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailingTransfer;

/**
 * @since 18/03/14
 * @author tgame14
 */
public class GridRailing extends TickingGrid<NodeRailing>
{
	public final static String CATEGORY_RAILING = "Item_Railings";

    public GridRailing (NodeRailing railing, Class type)
    {
        super(railing, type);
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

	public IItemRailing chooseNextInstantGoal(IItemRailingTransfer itemwrapper)
	{
		IItemRailing[] arr = (IItemRailing[]) itemwrapper.getRailing().getConnectionMap().entrySet().toArray();
		Arrays.sort(arr, new RailingDistanceComparator(itemwrapper.getEndGoal()));
		return arr[0];
	}

	public void onItemEnterGrid(IItemRailing railing, ItemStack item)
	{

	}
}

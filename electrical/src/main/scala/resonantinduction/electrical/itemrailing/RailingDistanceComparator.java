package resonantinduction.electrical.itemrailing;

import calclavia.lib.render.EnumColor;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailing;

import java.util.Comparator;

/**
 * @author tgame14
 * @since 16/04/14
 */
public class RailingDistanceComparator implements Comparator<IItemRailing>
{

	@Override
	public int compare(IItemRailing o1, IItemRailing o2)
	{
		return (int) o1.getWorldPos().floor().distance(o2.getWorldPos());
	}

	public static class RailingInventoryDistanceComparator extends RailingDistanceComparator
	{
		@Override
		public int compare(IItemRailing o1, IItemRailing o2)
		{
			if (o1.getInventoriesNearby() != null && o2.getInventoriesNearby() != null)
			{
				return super.compare(o1, o2);
			}

			else if (o1.getInventoriesNearby() == null && o2.getInventoriesNearby() == null)
			{
				return super.compare(o1, o2);
			}

			if (o1.getInventoriesNearby() != null)
			{
				return 1;
			}
			return -1;
		}
	}

	public static class RailingColoredDistanceComparator extends RailingDistanceComparator
	{
		private EnumColor color;

		public RailingColoredDistanceComparator(EnumColor color)
		{
			this.color = color;
		}

		@Override
		public int compare(IItemRailing o1, IItemRailing o2)
		{
			if (o1.getRailingColor() == o2.getRailingColor())
			{
				return super.compare(o1, o2);
			}

			else if (color == o1.getRailingColor())
			{
				return 1;
			}

			else if (color == o2.getRailingColor())
			{
				return -1;
			}

			return super.compare(o1, o2);
		}
	}
}

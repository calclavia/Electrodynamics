package resonantinduction.electrical.itemrailing.interfaces;

import net.minecraft.item.ItemStack;
import resonantinduction.electrical.itemrailing.PartRailing;
import calclavia.lib.render.EnumColor;

/**
 * the object that handles the items that flow through Railings
 *
 * @since 16/03/14
 * @author tgame14
 */
public interface IItemRailingTransfer
{
    public ItemStack getItemStack();

    public EnumColor getColor();

    public IItemRailingTransfer setColor(EnumColor color);

    public PartRailing getRailing();

    public IItemRailingTransfer setRailing(PartRailing railing);

    public IItemRailing getEndGoal();

	public IItemRailingTransfer setEndGoal(IItemRailing goal);
}

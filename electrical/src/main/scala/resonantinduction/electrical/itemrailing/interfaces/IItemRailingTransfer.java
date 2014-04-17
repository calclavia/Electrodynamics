package resonantinduction.electrical.itemrailing.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.electrical.itemrailing.PartRailing;
import calclavia.lib.render.EnumColor;

import java.util.Map;

/**
 * the object that functions as a Wrapper for items and handles the items that flow through Railings
 *
 * @since 16/03/14
 * @author tgame14
 */
public interface IItemRailingTransfer
{
    public ItemStack getItemStack();

    public EnumColor getColor();

    public IItemRailingTransfer setColor(EnumColor color);

    public IItemRailing getRailing();

    public IItemRailingTransfer setRailing(IItemRailing railing);

    public IItemRailing getEndGoal();

	public IItemRailingTransfer setEndGoal(IItemRailing goal);
}

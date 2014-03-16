package resonantinduction.electrical.itemrailing.interfaces;

import calclavia.lib.render.EnumColor;
import net.minecraft.item.ItemStack;
import resonantinduction.electrical.itemrailing.PartRailing;
import universalelectricity.api.vector.VectorWorld;

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

    public IRailing getEndGoal();

    public IItemRailingTransfer setEndGoal();
}

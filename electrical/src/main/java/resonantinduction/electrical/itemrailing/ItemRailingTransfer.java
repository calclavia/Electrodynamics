package resonantinduction.electrical.itemrailing;

import calclavia.lib.render.EnumColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailingTransfer;
import universalelectricity.api.vector.VectorWorld;

/**
 * An object that Transfers all
 *
 * @since 16/03/14
 * @author tgame14
 */
public class ItemRailingTransfer implements IItemRailingTransfer
{
    private ItemStack stack;
    private EnumColor color;
    private PartRailing railing;

    public ItemRailingTransfer(ItemStack stack, PartRailing railing)
    {
        this.stack = stack.copy();
        this.color = EnumColor.ORANGE;
        this.railing = railing;
    }

    public ItemRailingTransfer(Item item, PartRailing railing)
    {
        this(new ItemStack(item), railing);
    }

    @Override
    public ItemStack getItemStack ()
    {
        return this.stack;
    }

    @Override
    public EnumColor getColor ()
    {
        return this.color;
    }

    public IItemRailingTransfer setColor(EnumColor color)
    {
        this.color = color;
        return this;
    }

    @Override
    public PartRailing getRailing ()
    {
        return this.railing;
    }

    @Override
    public IItemRailingTransfer setRailing (PartRailing railing)
    {
        this.railing = railing;
        return this;
    }
}

package resonantinduction.electrical.itemrailing;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailing;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailingTransfer;
import calclavia.lib.render.EnumColor;

/**
 * An object that is a wrapper for all items through railings
 * 
 * @since 16/03/14
 * @author tgame14
 */
public class ItemRailingTransfer implements IItemRailingTransfer
{
	private ItemStack stack;
	private EnumColor color;
	private IItemRailing railing;

	public ItemRailingTransfer(ItemStack stack, PartRailing railing)
	{
		this.stack = stack.copy();
		this.color = EnumColor.ORANGE;
		this.railing = railing.getNode();
	}

	public ItemRailingTransfer(Item item, PartRailing railing)
	{
		this(new ItemStack(item), railing);
	}

	@Override
	public ItemStack getItemStack()
	{
		return this.stack;
	}

	@Override
	public EnumColor getColor()
	{
		return this.color;
	}

	public IItemRailingTransfer setColor(EnumColor color)
	{
		this.color = color;
		return this;
	}

	@Override
	public IItemRailing getRailing()
	{
		return this.railing;
	}

	@Override
	public IItemRailingTransfer setRailing(IItemRailing railing)
	{
		this.railing = railing;
		return this;
	}

	@Override
	public IItemRailing getEndGoal()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IItemRailingTransfer setEndGoal(IItemRailing goal)
	{
		// TODO Auto-generated method stub
		return null;
	}
}

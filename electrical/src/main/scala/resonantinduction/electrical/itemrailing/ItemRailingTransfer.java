package resonantinduction.electrical.itemrailing;

import calclavia.lib.render.EnumColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailing;
import resonantinduction.electrical.itemrailing.interfaces.IItemRailingTransfer;

import java.lang.ref.WeakReference;

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
	private WeakReference<IItemRailing> endTarget = null;

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
		return endTarget.get();
	}

	@Override
	public IItemRailingTransfer setEndGoal(IItemRailing goal)
	{
		this.endTarget = new WeakReference<IItemRailing>(goal);
		return this;
	}
}

package mffs.item.card;

import icbm.api.IItemFrequency;

import java.util.List;

import mffs.card.ItemCard;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemCardFrequency extends ItemCard implements IItemFrequency
{
	public ItemCardFrequency(int i)
	{
		super(i, "cardFrequency");
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer par2EntityPlayer, List list, boolean par4)
	{
		list.add("Frequency: " + this.getFrequency(itemStack));
	}

	@Override
	public int getFrequency(ItemStack itemStack)
	{
		if (itemStack != null)
		{
			if (itemStack.getTagCompound() == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}

			return itemStack.getTagCompound().getInteger("frequency");
		}

		return 0;
	}

	@Override
	public void setFrequency(int frequency, ItemStack itemStack)
	{
		if (itemStack != null)
		{
			if (itemStack.getTagCompound() == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}

			itemStack.getTagCompound().setInteger("frequency", frequency);
		}
	}
}
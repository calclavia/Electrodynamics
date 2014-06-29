package mffs.item.card;

import resonant.api.mffs.card.ICard;
import mffs.base.ItemMFFS;

public class ItemCard extends ItemMFFS implements ICard
{
	public ItemCard(int id, String name)
	{
		super(id, name);
		this.setMaxStackSize(1);
	}
}
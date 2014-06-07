package mffs.card;

import mffs.base.ItemMFFS;
import calclavia.api.mffs.card.ICard;

public class ItemCard extends ItemMFFS implements ICard
{
	public ItemCard(int id, String name)
	{
		super(id, name);
		this.setMaxStackSize(1);
	}
}
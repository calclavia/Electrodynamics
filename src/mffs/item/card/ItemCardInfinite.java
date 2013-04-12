package mffs.card;

import mffs.api.card.ICardInfinite;

/**
 * A card used by admins or players to cheat infinite energy.
 * 
 * @author Calclavia
 * 
 */
public class ItemCardInfinite extends ItemCard implements ICardInfinite
{
	public ItemCardInfinite(int id)
	{
		super(id, "cardInfinite");
	}
}
package mffs.api.card;

import net.minecraft.item.Item;
import resonantengine.lib.transform.vector.VectorWorld;

public interface ICoordLink {
	public void setLink(Item Item, VectorWorld position);

	public VectorWorld getLink(Item Item);
}

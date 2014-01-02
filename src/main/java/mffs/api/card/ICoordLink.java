package mffs.api.card;

import net.minecraft.item.ItemStack;
import universalelectricity.api.vector.VectorWorld;

public interface ICoordLink
{
	public void setLink(ItemStack itemStack, VectorWorld position);

	public VectorWorld getLink(ItemStack itemStack);
}

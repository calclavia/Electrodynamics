package mffs.api.card;

import com.resonant.access.AbstractAccess;
import net.minecraft.item.ItemStack;

/**
 * Applied to Item id and group cards.
 * @author Calclavia
 */
public interface IAccessCard extends ICard {
	public AbstractAccess getAccess(ItemStack stack);

	public void setAccess(ItemStack stack, AbstractAccess access);
}

package mffs.api.card;

import com.resonant.access.AbstractAccess;
import net.minecraft.item.Item;

/**
 * Applied to Item id and group cards.
 * @author Calclavia
 */
public interface IAccessCard extends ICard {
	public AbstractAccess getAccess(Item stack);

	public void setAccess(Item stack, AbstractAccess access);
}

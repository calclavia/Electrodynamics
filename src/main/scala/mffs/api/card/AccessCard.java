package mffs.api.card;

import com.resonant.core.access.AbstractAccess;

/**
 * Applied to Item id and group cards.
 *
 * @author Calclavia
 */
public interface AccessCard extends Card {
	public AbstractAccess getAccess();

	public void setAccess(AbstractAccess access);
}

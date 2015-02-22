package mffs.security.card

import java.util.{Set => JSet}

import com.resonant.core.access.AbstractAccess
import mffs.api.card.AccessCard
import mffs.item.card.ItemCard
import nova.core.util.components.{Storable, Stored}

/**
 * @author Calclavia
 */
abstract class ItemCardAccess extends ItemCard with AccessCard with Storable {

	@Stored
	var access: AbstractAccess

	def setAccess(access: AbstractAccess) {
		this.access = access
	}
}

package mffs.security.card

import java.util.{Set => JSet}

import com.resonant.core.access.AbstractAccess
import mffs.api.card.AccessCard
import mffs.item.card.ItemCard
import nova.core.retention.Storable

/**
 * @author Calclavia
 */
abstract class ItemCardAccess extends ItemCard with AccessCard with Storable {

	var access: AbstractAccess
}

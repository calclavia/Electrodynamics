package mffs.security.card

import java.util.{Set => JSet}

import com.resonant.core.access.AbstractAccess
import mffs.api.card.AccessCard
import mffs.item.card.ItemCard
import nova.core.retention.{Storable, Stored}

import scala.beans.BeanProperty

/**
 * @author Calclavia
 */
abstract class ItemCardAccess extends ItemCard with AccessCard with Storable {

	@Stored
	@BeanProperty
	var access: AbstractAccess
}

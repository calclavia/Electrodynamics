package com.calclavia.edx.optics.security.card

import java.util.{Set => JSet}

import com.calclavia.edx.optics.api.card.AccessCard
import com.calclavia.edx.optics.item.card.ItemCard
import com.resonant.core.access.AbstractAccess
import nova.core.retention.Storable

/**
 * @author Calclavia
 */
abstract class ItemCardAccess extends ItemCard with AccessCard with Storable {

	var access: AbstractAccess
}

package mffs.item.fortron

import java.util.Optional

import mffs.item.card.ItemCard
import nova.core.fluid.{Tank, TankProvider, TankSimple}

/**
 * A card used by admins or players to cheat infinite energy.
 *
 * @author Calclavia
 */
class ItemCardInfinite extends ItemCard with TankProvider {
	val tank = new TankSimple

	override def getTank: Optional[Tank] = Optional.of(tank)
}
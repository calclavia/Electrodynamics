package mffs.item.fortron

import java.util.Optional

import mffs.item.card.ItemCard
import nova.core.fluid.{FluidContainerProvider, FluidTankSimple, Tank}

/**
 * A card used by admins or players to cheat infinite energy.
 *
 * @author Calclavia
 */
class ItemCardInfinite extends ItemCard with FluidContainerProvider {
	val tank = new FluidTankSimple

	override def getTank: Optional[Tank] = Optional.of(tank)
}
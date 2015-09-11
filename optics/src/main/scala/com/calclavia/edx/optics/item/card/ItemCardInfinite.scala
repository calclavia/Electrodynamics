package com.calclavia.edx.optics.item.card

import nova.core.component.fluid.{Fluid, FluidHandler, TankSimple}

/**
 * A card used by admins or players to cheat infinite energy.
 *
 * @author Calclavia
 */
class ItemCardInfinite extends ItemCard {
	val tank = new TankSimple(Fluid.bucketVolume)

	add(new FluidHandler(tank))
}
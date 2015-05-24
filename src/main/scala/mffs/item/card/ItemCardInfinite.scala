package mffs.item.card

import nova.core.fluid.{Fluid, TankSimple}

/**
 * A card used by admins or players to cheat infinite energy.
 *
 * @author Calclavia
 */
class ItemCardInfinite extends ItemCard {
	val tank = new TankSimple(Fluid.bucketVolume)

	add(tank)

	override def getID: String = "cardInfinite"
}
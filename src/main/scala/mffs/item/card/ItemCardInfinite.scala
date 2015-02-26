package mffs.item.card

import java.util

import nova.core.fluid.{Fluid, Tank, TankProvider, TankSimple}

import scala.collection.convert.wrapAll._

/**
 * A card used by admins or players to cheat infinite energy.
 *
 * @author Calclavia
 */
class ItemCardInfinite extends ItemCard with TankProvider {
	val tank = new TankSimple(Fluid.bucketVolume)

	override def getTanks: util.Set[Tank] = Set[Tank](tank)
}
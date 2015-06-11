package nova.energy

import nova.core.component.Component

/**
 * A node used to store energy.
 * @author Calclavia
 */
//TODO: Move to Energy API (if decided), change to component
class EnergyStorage extends Component with Stat[Double] {

	override protected[this] implicit def n: Numeric[Double] = Numeric.DoubleIsFractional
}
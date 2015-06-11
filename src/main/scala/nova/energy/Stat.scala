package nova.energy

import nova.core.retention.{Data, Storable}

/**
 * A stat is a type of positive numerical value that has a maximum value
 * @author Calclavia
 */

trait Stat[T] extends Ordered[T] with Storable {

	private var _value: T = n.zero
	private var _prevValue: T = n.zero
	private var _max: T = n.zero

	//def n = implicitly[Numeric[T]]

	protected[this] implicit def n: Numeric[T]

	def setValue(newVal: T) = value = newVal

	def isMax: Boolean = n.gteq(value, max)

	def setMax(newMax: T): this.type = {
		max = newMax
		return this
	}

	def isLastEmpty: Boolean = (prev == 0 && !isMin) || (n.gt(prev, n.zero) && isMin)

	def prev = _prevValue

	def isMin: Boolean = n.lteq(value, n.zero)

	/**
	 * Returns the difference between the maximum and current value.
	 */
	def remaining: T = n.min(max, value)

	def +(amount: T): T = n.plus(value, amount)

	def -(amount: T): T = n.minus(value, amount)

	def +=(amount: T): T = {
		value = n.plus(value, amount)
		return value
	}

	def -=(amount: T): T = {
		value = n.minus(value, amount)
		return value
	}

	override def compare(that: T): Int = n.compare(value, that)

	def value = _value

	def value_=(newValue: T) {
		_prevValue = _value
		_value = n.min(n.max(newValue, n.zero), max)
	}

	override def save(data: Data) {
		data.put("statMax", Double.box(n.toDouble(max)))
		data.put("statValue", Double.box(n.toDouble(value)))
	}

	override def load(data: Data) {
		max = data.getOrDefault("statMax", Double.box(0)).asInstanceOf[T]
		value = data.getOrDefault("statValue", Double.box(0)).asInstanceOf[T]
	}

	override def toString: String = getClass.getSimpleName + "[" + value + "/" + max + "]"

	def max = _max

	def max_=(newMax: T) {
		_max = newMax
		value = value
	}
}

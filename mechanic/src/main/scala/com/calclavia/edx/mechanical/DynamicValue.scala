package com.calclavia.edx.mechanical

case class DynamicValue[@specialized T](flat: T, dynamic: Seq[() => T])(implicit num: Numeric[T]) {
	def apply() = dynamic.foldLeft(flat)((prev, provider) => num.plus(prev, provider()))
}

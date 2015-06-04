package com.calclavia.edx.optics.field.mobilize

class DelayedEvent(var ticks: Int, val evtMethod: (() => Unit) = null) extends Runnable {

	override def run() {
		if (ticks == 0) {
			onEvent
		}

		ticks -= 1
	}

	protected def onEvent = evtMethod.apply()

	/**
	 * The higher the number, the higher the priority.
	 * @return
	 */
	def priority: Int = 0
}
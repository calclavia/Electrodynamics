package mffs.field.mobilize

class DelayedEvent(var ticks: Int, val evtMethod: (() => Unit) = null) extends Runnable {

	protected def onEvent = evtMethod.apply()

	override def run() {
		if (ticks == 0) {
			onEvent
		}

		ticks -= 1
	}

	/**
	 * The higher the number, the higher the priority.
	 * @return
	 */
	def priority: Int = 0
}
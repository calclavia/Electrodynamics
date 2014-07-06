package mffs.mobilize.event

class DelayedEvent(val handler: IDelayedEventHandler, var ticks: Int, val evtMethod: (() => Unit) = null)
{
  protected def onEvent = evtMethod.apply()

  def update()
  {
    this.ticks -= 1

    if (this.ticks <= 0)
    {
      this.onEvent
    }
  }

  /**
   * The higher the number, the higher the priority.
   * @return
   */
  def priority: Int = 0
}
package mffs.field.mobilize.event

class DelayedEvent(val handler: IDelayedEventHandler, var ticks: Int, val evtMethod: (() => Unit) = null)
{
  protected def onEvent = evtMethod.apply()

  def update()
  {
    if (ticks == 0)
    {
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
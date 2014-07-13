package mffs.security.card.gui

import org.lwjgl.input.Mouse

/**
 * @author Calclavia
 */
class GuiScroll(val height: Int)
{
  /**
   * A value between 0 and 1, indicating the scroll distance.
   */
  private var _currentScroll = 0f

  def currentScroll = _currentScroll

  def currentScroll_=(scroll: Float) = _currentScroll = Math.min(Math.max(scroll, 0), 1)

  /**
   * Handles mouse input.
   */
  def handleMouseInput()
  {
    var i = Mouse.getEventDWheel

    if (i != 0)
    {
      i = Math.min(Math.max(i, -1), 1)
      currentScroll = currentScroll - i.toFloat / height.toFloat
    }
  }
}

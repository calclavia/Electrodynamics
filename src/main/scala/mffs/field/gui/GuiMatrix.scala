package mffs.field.gui

import mffs.base.{GuiMFFS, TileFieldMatrix}
import resonant.lib.gui.GuiContainerBase.SlotType
import resonant.lib.utility.LanguageUtility
import universalelectricity.core.transform.region.Rectangle
import universalelectricity.core.transform.vector.Vector2

/**
 * Anything that has a field matrix within it.
 * @author Calclavia
 */
abstract class GuiMatrix(container: ContainerMatrix, tile: TileFieldMatrix) extends GuiMFFS(container, tile)
{
  val matrixCenter = container.matrixCenter

  def setupTooltips()
  {
    /**
     * Tooltips
     */
    val north = LanguageUtility.getLocal("gui.projector." + (if (tile.absoluteDirection) "north" else "front"))
    val south = LanguageUtility.getLocal("gui.projector." + (if (tile.absoluteDirection) "south" else "back"))
    val west = LanguageUtility.getLocal("gui.projector." + (if (tile.absoluteDirection) "west" else "left"))
    val east = LanguageUtility.getLocal("gui.projector." + (if (tile.absoluteDirection) "east" else "right"))
    val up = LanguageUtility.getLocal("gui.projector.up")
    val down = LanguageUtility.getLocal("gui.projector.down")

    //Mode
    drawSlot(matrixCenter.xi, matrixCenter.yi, SlotType.NONE, 1f, 0.4f, 0.4f)
    tooltips.put(new Rectangle(matrixCenter, 18), LanguageUtility.getLocal("gui.projector.mode"))

    //NORTH
    (1 to 2) foreach (i => tooltips.put(new Rectangle(new Vector2(matrixCenter.xi, matrixCenter.yi - 18 * i), 18), north))
    //SOUTH
    (1 to 2) foreach (i => tooltips.put(new Rectangle(new Vector2(matrixCenter.xi, matrixCenter.yi + 18 * i), 18), south))
    //EAST
    (1 to 2) foreach (i => tooltips.put(new Rectangle(new Vector2(matrixCenter.xi + 18 * i, matrixCenter.yi), 18), east))
    //WEST
    (1 to 2) foreach (i => tooltips.put(new Rectangle(new Vector2(matrixCenter.xi - 18 * i, matrixCenter.yi), 18), west))

    //UP
    tooltips.put(new Rectangle(new Vector2(matrixCenter.xi - 18, matrixCenter.yi - 18), 18), up)
    tooltips.put(new Rectangle(new Vector2(matrixCenter.xi + 18, matrixCenter.yi - 18), 18), up)

    //DOWN
    tooltips.put(new Rectangle(new Vector2(matrixCenter.xi - 18, matrixCenter.yi + 18), 18), down)
    tooltips.put(new Rectangle(new Vector2(matrixCenter.xi + 18, matrixCenter.yi + 18), 18), down)
  }

  def drawMatrix()
  {
    //Mode
    drawSlot(matrixCenter.xi, matrixCenter.yi, SlotType.NONE, 1f, 0.4f, 0.4f)

    //NORTH
    (1 to 2) foreach (i => drawSlot(matrixCenter.xi, matrixCenter.yi - 18 * i, SlotType.ARR_UP))
    //SOUTH
    (1 to 2) foreach (i => drawSlot(matrixCenter.xi, matrixCenter.yi + 18 * i, SlotType.ARR_DOWN))
    //EAST
    (1 to 2) foreach (i => drawSlot(matrixCenter.xi + 18 * i, matrixCenter.yi, SlotType.ARR_RIGHT))
    //WEST
    (1 to 2) foreach (i => drawSlot(matrixCenter.xi - 18 * i, matrixCenter.yi, SlotType.ARR_LEFT))

    //UP
    drawSlot(matrixCenter.xi - 18, matrixCenter.yi - 18, SlotType.ARR_UP_LEFT)
    drawSlot(matrixCenter.xi + 18, matrixCenter.yi - 18, SlotType.ARR_UP_RIGHT)
    //DOWN
    drawSlot(matrixCenter.xi - 18, matrixCenter.yi + 18, SlotType.ARR_DOWN_LEFT)
    drawSlot(matrixCenter.xi + 18, matrixCenter.yi + 18, SlotType.ARR_DOWN_RIGHT)

    //Draw non-directional slots
    for (x <- -2 to 2; y <- -2 to 2)
      if (new Vector2(x, y).magnitude > 2)
        drawSlot(matrixCenter.xi + 18 * x, matrixCenter.yi + 18 * y)
  }
}

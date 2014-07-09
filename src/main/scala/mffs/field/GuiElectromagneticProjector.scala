package mffs.field

import mffs.ModularForceFieldSystem
import mffs.base.{GuiMFFS, TilePacketType}
import mffs.render.button.GuiIcon
import net.minecraft.client.gui.GuiButton
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import org.lwjgl.opengl.GL11
import resonant.lib.gui.GuiContainerBase.SlotType
import resonant.lib.network.PacketTile
import resonant.lib.render.EnumColor
import resonant.lib.utility.LanguageUtility
import resonant.lib.wrapper.WrapList._
import universalelectricity.api.UnitDisplay
import universalelectricity.core.transform.region.Rectangle
import universalelectricity.core.transform.vector.Vector2

class GuiElectromagneticProjector(player: EntityPlayer, tile: TileElectromagneticProjector) extends GuiMFFS(new ContainerElectromagneticProjector(player, tile), tile)
{
  val matrixCenter = new Vector2(80, 55)

  override def initGui()
  {
    super.initGui()
    buttonList.add(new GuiIcon(1, this.width / 2 - 110, this.height / 2 - 82, null, new ItemStack(Items.compass)))

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

  override def updateScreen
  {
    super.updateScreen
    buttonList.get(1).asInstanceOf[GuiIcon].setIndex(if (tile.absoluteDirection) 1 else 0)
  }

  protected override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
    fontRendererObj.drawString(tile.getInventoryName, xSize / 2 - fontRendererObj.getStringWidth(tile.getInventoryName) / 2, 6, 4210752)

    GL11.glPushMatrix()
    GL11.glRotatef(-90, 0, 0, 1)
    fontRendererObj.drawString(this.tile.getDirection.name, -82, 10, 4210752)
    GL11.glPopMatrix()

    drawFortronText(x, y)
    fontRendererObj.drawString(EnumColor.RED + "-" + new UnitDisplay(UnitDisplay.Unit.LITER, tile.getFortronCost * 20).symbol() + "/s", 120, 119, 4210752)
    super.drawGuiContainerForegroundLayer(x, y)
  }

  protected override def drawGuiContainerBackgroundLayer(f: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(f, x, y)

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


    drawFrequencyGui()
  }

  protected override def actionPerformed(guiButton: GuiButton)
  {
    super.actionPerformed(guiButton)

    if (guiButton.id == 1)
    {
      ModularForceFieldSystem.packetHandler.sendToServer(new PacketTile(tile.asInstanceOf[TileEntity], TilePacketType.TOGGLE_MODE_4.id: Integer))
    }
  }

}
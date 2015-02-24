package mffs.base

import mffs.ModularForceFieldSystem
import mffs.render.button.GuiIcon

class GuiMFFS(container: Container, tile: BlockMFFS) extends GuiContainerBase(container)
{
  ySize = 217

  def this(container: Container) = this(container, null)

  override def initGui()
  {
    super.initGui
    buttonList.clear()

    //Activation button
	  buttonList.add(new GuiIcon(0, width / 2 - 110, height / 2 - 104, new Item(Blocks.torch), new Item(Blocks.redstone_torch)))
  }

  override def updateScreen()
  {
    super.updateScreen()

	  if (tile.isInstanceOf[BlockMFFS])
    {
      if (buttonList.size > 0 && this.buttonList.get(0) != null)
      {
        buttonList.get(0).asInstanceOf[GuiIcon].setIndex(if (tile.isRedstoneActive) 1 else 0)
      }
    }
  }

  protected override def actionPerformed(guiButton: GuiButton)
  {
    super.actionPerformed(guiButton)

    if (tile != null && guiButton.id == 0)
    {
		ModularForceFieldSystem.packetHandler.sendToServer(new PacketTile(tile, PacketBlock.toggleActivation.id: Integer))
    }
  }

  protected def drawFortronText(x: Int, y: Int)
  {
	  if (tile.isInstanceOf[BlockFortron])
    {
		val fortronTile = tile.asInstanceOf[BlockFortron]
		drawTextWithTooltip("fortron", EnumColor.WHITE + "" + new UnitDisplay(UnitDisplay.Unit.LITER, fortronTile.getFortron).symbol() + "/" + new
				UnitDisplay(UnitDisplay.Unit.LITER, fortronTile.getFortronCapacity).symbol(), 35, 119, x, y)
    }
  }

  protected def drawFrequencyGui()
  {
    //Frequency Card
    drawSlot(7, 113)

	  if (tile.isInstanceOf[BlockFortron])
    {
		val fortronTile = tile.asInstanceOf[BlockFortron]

      //Fortron Bar
		drawLongBlueBar(30, 115, Math.min(fortronTile.getFortron.asInstanceOf[Float] / fortronTile.getFortronCapacity.asInstanceOf[Float], 1))
    }
  }

}
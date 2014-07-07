package mffs.mobilize

import mffs.ModularForceFieldSystem
import mffs.base.{GuiMFFS, TilePacketType}
import mffs.render.button.GuiIcon
import net.minecraft.client.gui.GuiButton
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.ItemStack
import resonant.lib.gui.GuiContainerBase
import resonant.lib.gui.GuiContainerBase.SlotType
import resonant.lib.network.PacketTile
import resonant.lib.utility.LanguageUtility
import resonant.lib.wrapper.WrapList._
import universalelectricity.api.UnitDisplay
import universalelectricity.core.transform.region.Rectangle
import universalelectricity.core.transform.vector.Vector2

class GuiForceMobilizer(player: EntityPlayer, tileEntity: TileForceMobilizer) extends GuiMFFS(new ContainerForceManipulator(player, tileEntity), tileEntity)
{
  override def initGui
  {
    this.textFieldPos = new Vector2(111, 93)
    super.initGui
    this.buttonList.add(new GuiIcon(1, this.width / 2 - 82, this.height / 2 - 16, new ItemStack(Items.clock)))
    this.buttonList.add(new GuiIcon(2, this.width / 2 - 82, this.height / 2 - 82, null, new ItemStack(Items.redstone), new ItemStack(Blocks.redstone_block)))
    this.buttonList.add(new GuiIcon(3, this.width / 2 - 82, this.height / 2 - 60, null, new ItemStack(Blocks.anvil)))
    this.buttonList.add(new GuiIcon(4, this.width / 2 - 82, this.height / 2 - 38, null, new ItemStack(Items.compass)))
    this.tooltips.put(new Rectangle(new Vector2(117, 44), new Vector2(117, 44).add(18)), LanguageUtility.getLocal("gui.projector.mode"))
    this.tooltips.put(new Rectangle(new Vector2(90, 17), new Vector2(90, 17).add(18)), LanguageUtility.getLocal("gui.projector.up"))
    this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 3, 17), new Vector2(90 + 18 * 3, 17).add(18)), LanguageUtility.getLocal("gui.projector.up"))
    this.tooltips.put(new Rectangle(new Vector2(90, 17 + 18 * 3), new Vector2(90, 17 + 18 * 3).add(18)), LanguageUtility.getLocal("gui.projector.down"))
    this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 3, 17 + 18 * 3), new Vector2(90 + 18 * 3, 17 + 18 * 3).add(18)), LanguageUtility.getLocal("gui.projector.down"))
    var north: String = LanguageUtility.getLocal("gui.projector.north")
    var south: String = LanguageUtility.getLocal("gui.projector.south")
    var west: String = LanguageUtility.getLocal("gui.projector.west")
    var east: String = LanguageUtility.getLocal("gui.projector.east")

    if (!tileEntity.absoluteDirection)
    {
      north = LanguageUtility.getLocal("gui.projector.front")
      south = LanguageUtility.getLocal("gui.projector.back")
      west = LanguageUtility.getLocal("gui.projector.left")
      east = LanguageUtility.getLocal("gui.projector.right")
    }

    this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 1, 17), new Vector2(90 + 18 * 1, 17).add(18)), north)
    this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 2, 17), new Vector2(90 + 18 * 2, 17).add(18)), north)
    this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 1, 17 + 18 * 3), new Vector2(90 + 18 * 1, 17 + 18 * 3).add(18)), south)
    this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 2, 17 + 18 * 3), new Vector2(90 + 18 * 2, 17 + 18 * 3).add(18)), south)
    this.tooltips.put(new Rectangle(new Vector2(90, 17 + 18 * 1), new Vector2(90 + 18 * 1, 17 + 18 * 1).add(18)), west)
    this.tooltips.put(new Rectangle(new Vector2(90, 17 + 18 * 2), new Vector2(90 + 18 * 1, 17 + 18 * 2).add(18)), west)
    this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 3, 17 + 18 * 1), new Vector2(90 + 18 * 3, 17 + 18 * 1).add(18)), east)
    this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 3, 17 + 18 * 2), new Vector2(90 + 18 * 3, 17 + 18 * 2).add(18)), east)
  }

  protected override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
    fontRendererObj.drawString(tileEntity.getInventoryName, this.xSize / 2 - fontRendererObj.getStringWidth(tileEntity.getInventoryName) / 2, 6, 4210752)
    fontRendererObj.drawString(LanguageUtility.getLocal("gui.manipulator.anchor"), 35, 60, 4210752)
    if (this.tileEntity.anchor != null)
    {
      fontRendererObj.drawString(this.tileEntity.anchor.xi + ", " + this.tileEntity.anchor.yi + ", " + this.tileEntity.anchor.zi, 35, 70, 4210752)
    }
    fontRendererObj.drawString(this.tileEntity.getDirection.name, 35, 82, 4210752)
    fontRendererObj.drawString((this.tileEntity.clientMoveTime / 20) + "s", 35, 94, 4210752)
    this.textFieldFrequency.drawTextBox
    drawTextWithTooltip("fortron", "\u00a74Consumption: -" + new UnitDisplay(UnitDisplay.Unit.LITER, tileEntity.getFortronCost * 20).simple() + "/s", 30, 110, x, y)
    drawTextWithTooltip("fortron", "\u00a7F" + new UnitDisplay(UnitDisplay.Unit.LITER, tileEntity.getFortronEnergy).simple() + "/" + new UnitDisplay(UnitDisplay.Unit.LITER, tileEntity.getFortronCapacity).simple(), 68, 122, x, y)
    fontRendererObj.drawString(LanguageUtility.getLocal("gui.manipulator.fortron"), 8, 121, 4210752)
    super.drawGuiContainerForegroundLayer(x, y)
  }

  override def updateScreen
  {
    super.updateScreen
    (this.buttonList.get(2).asInstanceOf[GuiIcon]).setIndex(this.tileEntity.displayMode)
    (this.buttonList.get(3).asInstanceOf[GuiIcon]).setIndex(if (this.tileEntity.doAnchor) 1 else 0)
    (this.buttonList.get(4).asInstanceOf[GuiIcon]).setIndex(if (this.tileEntity.absoluteDirection) 1 else 0)
  }

  override def drawGuiContainerBackgroundLayer(f: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(f, x, y)
    drawSlot(72, 90)
    drawSlot(72 + 18, 90)
    drawSlot(117, 44, SlotType.NONE, 1f, 0.4f, 0.4f)

    (0 until 4) foreach (xSlot =>
    {
      (0 until 4) foreach (ySlot =>
      {
        if (!(xSlot == 1 && ySlot == 1) && !(xSlot == 2 && ySlot == 2) && !(xSlot == 1 && ySlot == 2) && !(xSlot == 2 && ySlot == 1))
        {
          var slotType: GuiContainerBase.SlotType = SlotType.NONE

          if (xSlot == 0 && ySlot == 0)
          {
            slotType = SlotType.ARR_UP_LEFT
          }
          else if (xSlot == 0 && ySlot == 3)
          {
            slotType = SlotType.ARR_DOWN_LEFT
          }
          else if (xSlot == 3 && ySlot == 0)
          {
            slotType = SlotType.ARR_UP_RIGHT
          }
          else if (xSlot == 3 && ySlot == 3)
          {
            slotType = SlotType.ARR_DOWN_RIGHT
          }
          else if (ySlot == 0)
          {
            slotType = SlotType.ARR_UP
          }
          else if (ySlot == 3)
          {
            slotType = SlotType.ARR_DOWN
          }
          else if (xSlot == 0)
          {
            slotType = SlotType.ARR_LEFT
          }
          else if (xSlot == 3)
          {
            slotType = SlotType.ARR_RIGHT
          }
          this.drawSlot(90 + 18 * xSlot, 17 + 18 * ySlot, slotType)
        }
      })
    })


    var xSlot: Int = 0
    while (xSlot < 3)
    {

      var ySlot: Int = 0
      while (ySlot < 2)
      {

        this.drawSlot(30 + 18 * xSlot, 18 + 18 * ySlot)
        ySlot += 1
      }


      xSlot += 1
    }

    this.drawForce(60, 120, Math.min(this.tileEntity.getFortronEnergy.asInstanceOf[Float] / this.tileEntity.getFortronCapacity.asInstanceOf[Float], 1))
  }

  override def actionPerformed(guiButton: GuiButton)
  {
    super.actionPerformed(guiButton)
    if (guiButton.id == 1)
    {

      ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(tileEntity, TilePacketType.TOGGLE_MODE.id: Integer))
    }
    else if (guiButton.id == 2)
    {
      ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(tileEntity, TilePacketType.TOGGLE_MODE_2.id: Integer))
    }
    else if (guiButton.id == 3)
    {
      ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(tileEntity, TilePacketType.TOGGLE_MODE_3.id: Integer))
    }
    else if (guiButton.id == 4)
    {
      ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(tileEntity, TilePacketType.TOGGLE_MODE_4.id: Integer))
    }
  }
}
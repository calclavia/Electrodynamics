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
import resonant.lib.utility.LanguageUtility
import resonant.lib.wrapper.WrapList._
import universalelectricity.api.UnitDisplay
import universalelectricity.core.transform.region.Rectangle
import universalelectricity.core.transform.vector.Vector2

class GuiElectromagneticProjector(player: EntityPlayer, tile: TileElectromagneticProjector) extends GuiMFFS(new ContainerForceFieldProjector(player, tile), tile)
{
  override def initGui
  {
    this.textFieldPos = new Vector2(48, 91)
    super.initGui
    this.buttonList.add(new GuiIcon(1, this.width / 2 - 110, this.height / 2 - 82, null, new ItemStack(Items.compass)))

    this.tooltips.put(new Rectangle(new Vector2(117, 44), new Vector2(117, 44).add(18)), LanguageUtility.getLocal("gui.projector.mode"))
    this.tooltips.put(new Rectangle(new Vector2(90, 17), new Vector2(90, 17).add(18)), LanguageUtility.getLocal("gui.projector.up"))
    this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 3, 17), new Vector2(90 + 18 * 3, 17).add(18)), LanguageUtility.getLocal("gui.projector.up"))
    this.tooltips.put(new Rectangle(new Vector2(90, 17 + 18 * 3), new Vector2(90, 17 + 18 * 3).add(18)), LanguageUtility.getLocal("gui.projector.down"))
    this.tooltips.put(new Rectangle(new Vector2(90 + 18 * 3, 17 + 18 * 3), new Vector2(90 + 18 * 3, 17 + 18 * 3).add(18)), LanguageUtility.getLocal("gui.projector.down"))
    var north: String = LanguageUtility.getLocal("gui.projector.north")
    var south: String = LanguageUtility.getLocal("gui.projector.south")
    var west: String = LanguageUtility.getLocal("gui.projector.west")
    var east: String = LanguageUtility.getLocal("gui.projector.east")

    if (!tile.absoluteDirection)
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

  override def updateScreen
  {
    super.updateScreen
    (this.buttonList.get(1).asInstanceOf[GuiIcon]).setIndex(if (tile.absoluteDirection) 1 else 0)
  }

  protected override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
    fontRendererObj.drawString(tile.getInventoryName, xSize / 2 - fontRendererObj.getStringWidth(tile.getInventoryName) / 2, 6, 4210752)
    GL11.glPushMatrix
    GL11.glRotatef(-90, 0, 0, 1)
    fontRendererObj.drawString(this.tile.getDirection.name, -82, 10, 4210752)
    GL11.glPopMatrix
    this.textFieldFrequency.drawTextBox
    this.drawTextWithTooltip("fortron", "%1: " + new UnitDisplay(UnitDisplay.Unit.LITER, tile.getFortronEnergy).symbol() + "/" + new UnitDisplay(UnitDisplay.Unit.LITER, tile.getFortronCapacity).symbol(), 8, 110, x, y)
    fontRendererObj.drawString("\u00a74-" + new UnitDisplay(UnitDisplay.Unit.LITER, tile.getFortronCost * 20).symbol() + "/s", 118, 121, 4210752)
    super.drawGuiContainerForegroundLayer(x, y)
  }

  protected override def drawGuiContainerBackgroundLayer(f: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(f, x, y)
    /*drawSlot(9, 88)
    drawSlot(9 + 18, 88)*/

    //Mode
    drawSlot(xSize / 2 - 16 / 2, 44, SlotType.NONE, 1f, 0.4f, 0.4f)

    (0 until 4)
    /*
    {
      var xSlot: Int = 0
      while (xSlot < 4)
      {
        {
          {
            var ySlot: Int = 0
            while (ySlot < 4)
            {
              {
                if (!(xSlot == 1 && ySlot == 1) && !(xSlot == 2 && ySlot == 2) && !(xSlot == 1 && ySlot == 2) && !(xSlot == 2 && ySlot == 1))
                {
                  var `type`: GuiContainerBase.SlotType = SlotType.NONE
                  if (xSlot == 0 && ySlot == 0)
                  {
                    `type` = SlotType.ARR_UP_LEFT
                  }
                  else if (xSlot == 0 && ySlot == 3)
                  {
                    `type` = SlotType.ARR_DOWN_LEFT
                  }
                  else if (xSlot == 3 && ySlot == 0)
                  {
                    `type` = SlotType.ARR_UP_RIGHT
                  }
                  else if (xSlot == 3 && ySlot == 3)
                  {
                    `type` = SlotType.ARR_DOWN_RIGHT
                  }
                  else if (ySlot == 0)
                  {
                    `type` = SlotType.ARR_UP
                  }
                  else if (ySlot == 3)
                  {
                    `type` = SlotType.ARR_DOWN
                  }
                  else if (xSlot == 0)
                  {
                    `type` = SlotType.ARR_LEFT
                  }
                  else if (xSlot == 3)
                  {
                    `type` = SlotType.ARR_RIGHT
                  }
                  this.drawSlot(90 + 18 * xSlot, 17 + 18 * ySlot, `type`)
                }
              }
              ({
                ySlot += 1;
                ySlot - 1
              })
            }
          }
        }
        ({
          xSlot += 1;
          xSlot - 1
        })
      }
    }
    {
      var xSlot: Int = 0
      while (xSlot < 3)
      {
        {
          {
            var ySlot: Int = 0
            while (ySlot < 2)
            {
              {
                this.drawSlot(30 + 18 * xSlot, 35 + 18 * ySlot)
              }
              ({
                ySlot += 1;
                ySlot - 1
              })
            }
          }
        }
        ({
          xSlot += 1;
          xSlot - 1
        })
      }
    }
    */
    drawForce(8, 120, Math.min(this.tile.getFortronEnergy.asInstanceOf[Float] / this.tile.getFortronCapacity.asInstanceOf[Float], 1))
  }

  protected override def actionPerformed(guiButton: GuiButton)
  {
    super.actionPerformed(guiButton)

    if (guiButton.id == 1)
    {
      ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(tile.asInstanceOf[TileEntity], TilePacketType.TOGGLE_MODE_4.id: Integer))
    }
  }

}
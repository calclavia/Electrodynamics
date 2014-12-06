/**
 *
 */
package resonantinduction.electrical.multimeter

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.gui.{GuiButton, GuiTextField}
import net.minecraft.entity.player.InventoryPlayer
import resonant.lib.prefab.gui.GuiContainerBase
import resonant.lib.render.EnumColor
import resonant.lib.utility.LanguageUtility
import resonant.lib.wrapper.WrapList._

/**
 * Multimeter GUI
 *
 * @author Calclavia
 *
 */
@SideOnly(Side.CLIENT)
class GuiMultimeter(inventoryPlayer: InventoryPlayer, multimeter: PartMultimeter) extends GuiContainerBase(new ContainerMultimeter(inventoryPlayer, multimeter))
{
  private var textFieldLimit: GuiTextField = null
  this.ySize = 217

  override def initGui
  {
    super.initGui
    this.buttonList.add(new GuiButton(0, this.width / 2 + 20, this.height / 2 - 23, 50, 20, LanguageUtility.getLocal("gui.resonantinduction.multimeter.toggle")))
    this.buttonList.add(new GuiButton(1, this.width / 2 - 80, this.height / 2 - 75, 100, 20, LanguageUtility.getLocal("gui.resonantinduction.multimeter.toggleDetection")))
    this.buttonList.add(new GuiButton(2, this.width / 2 - 80, this.height / 2 + 0, 80, 20, LanguageUtility.getLocal("gui.resonantinduction.multimeter.toggleGraph")))
    this.textFieldLimit = new GuiTextField(fontRendererObj, 9, 90, 90, 12)
    this.textFieldLimit.setMaxStringLength(8)
    this.textFieldLimit.setText("" + this.multimeter.redstoneTriggerLimit)
  }

  protected override def keyTyped(par1: Char, par2: Int)
  {
    super.keyTyped(par1, par2)
    this.textFieldLimit.textboxKeyTyped(par1, par2)

    try
    {
      multimeter.redstoneTriggerLimit = textFieldLimit.getText.toDouble
      multimeter.updateServer
    }
    catch
      {
        case e: Exception =>
        {
        }
      }
  }

  protected override def mouseClicked(par1: Int, par2: Int, par3: Int)
  {
    super.mouseClicked(par1, par2, par3)
    this.textFieldLimit.mouseClicked(par1 - this.containerWidth, par2 - this.containerHeight, par3)
  }

  protected override def actionPerformed(button: GuiButton)
  {
    button.id match
    {
      case 0 =>
        multimeter.toggleMode
      case 1 =>
        multimeter.toggleDetectionValue
      case 2 =>
        multimeter.toggleGraphType
    }
  }

  protected override def drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int)
  {
    val graphName: String = multimeter.getNetwork.getLocalized(multimeter.getNetwork.graphs(multimeter.graphType))
    super.drawGuiContainerForegroundLayer(mouseX, mouseY)
    val s: String = LanguageUtility.getLocal("item.resonantinduction:multimeter.name")
    this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752)
    this.fontRendererObj.drawString(EnumColor.INDIGO + "Detection Type", 9, 20, 4210752)
    this.fontRendererObj.drawString(multimeter.getNetwork.getDisplay(multimeter.detectType), 9, 60, 4210752)
    this.fontRendererObj.drawString(LanguageUtility.getLocal("gui.resonantinduction.multimeter.logic") + " " + EnumColor.RED + LanguageUtility.getLocal("gui.resonantinduction.multimeter." + this.multimeter.getMode.display), 9, 75, 4210752)
    this.fontRendererObj.drawString(graphName, 95, 115, 4210752)
    this.textFieldLimit.drawTextBox()
  }
}
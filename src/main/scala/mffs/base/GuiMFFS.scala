package mffs.base

import mffs.gui.button.GuiIcon
import mffs.{ModularForceFieldSystem, Settings}
import net.minecraft.client.gui.{GuiButton, GuiTextField}
import net.minecraft.init.Blocks
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import org.lwjgl.input.Keyboard
import resonant.api.blocks.IBlockFrequency
import resonant.api.mffs.IBiometricIdentifierLink
import resonant.lib.gui.GuiContainerBase
import resonant.lib.network.PacketTile
import resonant.lib.utility.LanguageUtility
import universalelectricity.core.transform.vector.Vector2

class GuiMFFS(container: Container, frequencyTile: IBlockFrequency) extends GuiContainerBase(container)
{
  /**
   * Frequency Text Field
   */
  protected var textFieldFrequency: GuiTextField = null
  protected var textFieldPos: Vector2 = new Vector2()

  ySize = 217

  def this(container: Container) = this(container, null)

  override def initGui()
  {
    super.initGui
    this.buttonList.clear
    this.buttonList.add(new GuiIcon(0, this.width / 2 - 82, this.height / 2 - 104, new ItemStack(Blocks.unlit_redstone_torch), new ItemStack(Blocks.redstone_torch)))
    Keyboard.enableRepeatEvents(true)
    if (this.frequencyTile != null)
    {
      this.textFieldFrequency = new GuiTextField(this.fontRendererObj, this.textFieldPos.xi, this.textFieldPos.yi, 50, 12)
      this.textFieldFrequency.setMaxStringLength(Settings.maxFrequencyDigits)
      this.textFieldFrequency.setText(frequencyTile.getFrequency + "")
    }
  }

  protected override def keyTyped(par1: Char, par2: Int)
  {
    super.keyTyped(par1, par2)
    if (this.textFieldFrequency != null)
    {
      this.textFieldFrequency.textboxKeyTyped(par1, par2)
      try
      {
        val newFrequency: Int = Math.max(0, Integer.parseInt(this.textFieldFrequency.getText))
        this.frequencyTile.setFrequency(newFrequency)
        this.textFieldFrequency.setText(this.frequencyTile.getFrequency + "")
        ModularForceFieldSystem.packetHandler.sendToServer(new PacketTile(frequencyTile.asInstanceOf[TileEntity], Array(TileMFFS.TilePacketType.FREQUENCY.ordinal, frequencyTile.getFrequency(): Integer)))
      }
      catch
        {
          case e: NumberFormatException =>
          {
          }
        }
    }
  }

  protected override def actionPerformed(guiButton: GuiButton)
  {
    super.actionPerformed(guiButton)
    if (this.frequencyTile != null && guiButton.id == 0)
    {
      ModularForceFieldSystem.packetHandler.sendToServer(new PacketTile(frequencyTile.asInstanceOf[TileEntity], Array(TileMFFS.TilePacketType.TOGGLE_ACTIVATION.ordinal)))
    }
  }

  override def updateScreen()
  {
    super.updateScreen
    if (this.textFieldFrequency != null)
    {
      if (!this.textFieldFrequency.isFocused)
      {
        this.textFieldFrequency.setText(this.frequencyTile.getFrequency + "")
      }
    }
    if (this.frequencyTile.isInstanceOf[TileMFFS])
    {
      if (this.buttonList.size > 0 && this.buttonList.get(0) != null)
      {
        (this.buttonList.get(0).asInstanceOf[GuiIcon]).setIndex(if ((this.frequencyTile.asInstanceOf[TileMFFS]).isRedstoneActive) 1 else 0)
      }
    }
  }

  override def mouseClicked(x: Int, y: Int, par3: Int)
  {
    super.mouseClicked(x, y, par3)
    if (this.textFieldFrequency != null)
    {
      this.textFieldFrequency.mouseClicked(x - this.containerWidth, y - this.containerHeight, par3)
    }
  }

  protected override def drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int)
  {
    super.drawGuiContainerForegroundLayer(mouseX, mouseY)
    if (this.textFieldFrequency != null)
    {
      if (this.isPointInRegion(textFieldPos.xi, textFieldPos.yi, this.textFieldFrequency.getWidth, 12, mouseX, mouseY))
      {
        this.tooltip = LanguageUtility.getLocal("gui.frequency.tooltip")
      }
    }
  }

  protected override def drawGuiContainerBackgroundLayer(var1: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(var1, x, y)

    if (this.frequencyTile.isInstanceOf[IBiometricIdentifierLink])
    {
      this.drawBulb(167, 4, (this.frequencyTile.asInstanceOf[IBiometricIdentifierLink]).getBiometricIdentifier != null)
    }
  }

}
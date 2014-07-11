package mffs.security

import mffs.ModularForceFieldSystem
import mffs.base.{GuiMFFS, TilePacketType}
import net.minecraft.client.gui.{GuiButton, GuiTextField}
import net.minecraft.entity.player.EntityPlayer
import resonant.lib.network.discriminator.PacketTile
import universalelectricity.core.transform.vector.Vector2

class GuiBiometricIdentifier(player: EntityPlayer, tile: TileBiometricIdentifier) extends GuiMFFS(new ContainerBiometricIdentifier(player, tile), tile)
{
  private var textFieldUsername: GuiTextField = null

  override def initGui
  {
    super.initGui
    this.textFieldUsername = new GuiTextField(fontRendererObj, 52, 18, 90, 12)
    this.textFieldUsername.setMaxStringLength(30)
    /*
    var x: Int = 0
    var y: Int = 0
    {
      var i: Int = 0
      while (i < MFFSPermissions.getPermissions.length)
      {
        {
          x += 1
          this.buttonList.add(new GuiButtonPress(i + 1, this.width / 2 - 50 + 20 * x, this.height / 2 - 75 + 20 * y, new Vector2(18, 18 * i), this, MFFSPermissions.getPermissions(i).name))
          if (i % 3 == 0 && i != 0)
          {
            x = 0
            y += 1
          }
        }
        ({
          i += 1;
          i - 1
        })
      }
    }*/
  }

  protected override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
    fontRendererObj.drawString(tile.getInventoryName, this.xSize / 2 - fontRendererObj.getStringWidth(tile.getInventoryName) / 2, 6, 4210752)
    this.drawTextWithTooltip("rights", "%1", 8, 32, x, y, 0)
    /*
    try
    {
      if (this.tile.getEditCard != null)
      {
        val idCard: ICardIdentification = this.tile.getEditCard.getItem.asInstanceOf[ICardIdentification]
        this.textFieldUsername.drawTextBox
        if (idCard.getProfile(this.tile.getEditCard) != null)
        {
          {
            var i: Int = 0
            while (i < this.buttonList.size)
            {
              {
                if (this.buttonList.get(i).isInstanceOf[GuiButtonPress])
                {
                  val button: GuiButtonPress = this.buttonList.get(i).asInstanceOf[GuiButtonPress]
                  button.drawButton = true
                  val permissionID: Int = i - 1
                  if (MFFSPermissions.getPermission(permissionID) != null)
                  {
                    if (idCard.hasPermission(this.tile.getEditCard, MFFSPermissions.getPermission(permissionID)))
                    {
                      button.stuck = true
                    }
                    else
                    {
                      button.stuck = false
                    }
                  }
                }
              }
              ({
                i += 1;
                i - 1
              })
            }
          }
        }
      }
      else
      {
        for (button <- this.buttonList)
        {
          if (button.isInstanceOf[GuiButtonPress])
          {
            (button.asInstanceOf[GuiButtonPress]).drawButton = false
          }
        }
      }
    }
    catch
      {
        case e: Exception =>
        {
          e.printStackTrace
        }
      }
    */
    this.drawTextWithTooltip("master", 28, 90 + (fontRendererObj.FONT_HEIGHT / 2), x, y)
    super.drawGuiContainerForegroundLayer(x, y)
  }

  override def updateScreen
  {
    super.updateScreen
    /*
    if (!this.textFieldUsername.isFocused)
    {
      if (this.tile.getEditCard != null)
      {
        val idCard: ICardIdentification = this.tile.getEditCard.getItem.asInstanceOf[ICardIdentification]
        if (idCard.getProfile(this.tile.getEditCard) != null)
        {
          this.textFieldUsername.setText(idCard.getProfile(this.tile.getEditCard))
        }
      }
    }*/
  }

  protected override def drawGuiContainerBackgroundLayer(f: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(f, x, y)
    /*
    this.drawSlot(87, 90)
    this.drawSlot(7, 45)
    this.drawSlot(7, 65)
    this.drawSlot(7, 90)
    {
      var var4: Int = 0
      while (var4 < 9)
      {
        {
          this.drawSlot(8 + var4 * 18 - 1, 110)
        }
        ({
          var4 += 1;
          var4 - 1
        })
      }
    }*/
  }

  protected override def keyTyped(par1: Char, par2: Int)
  {

    if (par1 != 'e' && par1 != 'E')
    {
      super.keyTyped(par1, par2)
    }
    /*
    this.textFieldUsername.textboxKeyTyped(par1, par2)
    try
    {
      ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(this.tile, TilePacketType.STRING.id: Integer, this.textFieldUsername.getText))
    }
    catch
      {
        case e: NumberFormatException =>
        {
        }
      }*/
  }

  override def mouseClicked(x: Int, y: Int, par3: Int)
  {
    super.mouseClicked(x, y, par3)
    this.textFieldUsername.mouseClicked(x - containerWidth, y - containerHeight, par3)
  }

  protected override def actionPerformed(guiButton: GuiButton)
  {
    super.actionPerformed(guiButton)

    if (guiButton.id > 0)
    {
      ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(tile, TilePacketType.TOGGLE_MODE.id: Integer, (guiButton.id - 1): Integer))
    }
  }
}
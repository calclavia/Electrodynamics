package mffs.security

import mffs.base.GuiMFFS
import net.minecraft.entity.player.EntityPlayer
import resonant.lib.render.EnumColor

class GuiBiometricIdentifier(player: EntityPlayer, tile: TileBiometricIdentifier) extends GuiMFFS(new ContainerBiometricIdentifier(player, tile), tile)
{
  override def initGui
  {
    super.initGui
  }

  protected override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
    drawStringCentered(tile.getInventoryName)

    drawStringCentered(EnumColor.AQUA + "ID and Group Cards", 20)
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

    for (x <- 0 until 9; y <- 0 until 5)
      drawSlot(8 + x * 18, 35 + y * 18)
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
}
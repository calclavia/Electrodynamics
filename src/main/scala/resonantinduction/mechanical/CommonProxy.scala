package resonantinduction.mechanical

import cpw.mods.fml.common.network.IGuiHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World

class CommonProxy extends IGuiHandler {
  def preInit {
  }

  def init {
  }

  def postInit {
  }

  def getServerGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = {
    return null
  }

  def getClientGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = {
    return null
  }
}
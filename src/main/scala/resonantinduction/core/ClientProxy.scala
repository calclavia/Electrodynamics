package resonantinduction.core

import java.awt.Color

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.Block
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.particle.{EntityDiggingFX, EntityFX}
import net.minecraft.world.World
import universalelectricity.core.transform.vector.Vector3

/** @author Calclavia */
@SideOnly(Side.CLIENT) class ClientProxy extends CommonProxy
{
  override def isPaused: Boolean =
  {
    if (FMLClientHandler.instance.getClient.isSingleplayer && !FMLClientHandler.instance.getClient.getIntegratedServer.getPublic)
    {
      val screen: GuiScreen = FMLClientHandler.instance.getClient.currentScreen
      if (screen != null)
      {
        if (screen.doesGuiPauseGame)
        {
          return true
        }
      }
    }
    return false
  }

  override def isGraphicsFancy: Boolean =
  {
    return FMLClientHandler.instance.getClient.gameSettings.fancyGraphics
  }

  override def renderBlockParticle(world: World, position: Vector3, velocity: Vector3, blockID: Int, scale: Float)
  {
    this.renderBlockParticle(world, position.x, position.y, position.z, velocity, blockID, scale)
  }

  def renderBlockParticle(world: World, x: Double, y: Double, z: Double, velocity: Vector3, block: Block, scale: Float)
  {
    val fx: EntityFX = new EntityDiggingFX(world, x, y, z, velocity.x, velocity.y, velocity.z, block, 0, 0)
    fx.multipleParticleScaleBy(scale)
    fx.noClip = true
    FMLClientHandler.instance.getClient.effectRenderer.addEffect(fx)
  }

  override def renderBeam(world: World, position: Vector3, hit: Vector3, color: Color, age: Int)
  {
    renderBeam(world, position, hit, color.getRed, color.getGreen, color.getBlue, age)
  }

  override def renderBeam(world: World, position: Vector3, target: Vector3, red: Float, green: Float, blue: Float, age: Int)
  {
  }
}
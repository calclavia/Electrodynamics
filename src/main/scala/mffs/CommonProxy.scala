package mffs

import com.mojang.authlib.GameProfile
import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.network.IGuiHandler
import mffs.field.TileElectromagneticProjector
import mffs.field.gui.{GuiForceMobilizer, GuiElectromagneticProjector, ContainerMatrix}
import mffs.field.mobilize.TileForceMobilizer
import mffs.item.gui.{ContainerItem, ContainerFrequency}
import mffs.production._
import mffs.render.fx.IEffectController
import mffs.security.{GuiBiometricIdentifier, ContainerBiometricIdentifier, TileBiometricIdentifier}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.world.World
import resonant.lib.prefab.AbstractProxy
import resonant.lib.render.EnumColor
import universalelectricity.core.transform.vector.Vector3

class CommonProxy extends AbstractProxy with IGuiHandler
{
  override def getServerGuiElement(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef =
  {
    id match
    {
      case 0 =>
      {
        val tileEntity = world.getTileEntity(x, y, z)
        tileEntity match
        {
          case tile: TileFortronCapacitor => return new ContainerFortronCapacitor(player, tile)
          case tile: TileElectromagneticProjector => return new ContainerMatrix(player, tile)
          case tile: TileCoercionDeriver => return new ContainerCoercionDeriver(player, tile)
          case tile: TileBiometricIdentifier => return new ContainerBiometricIdentifier(player, tile)
          case tile: TileForceMobilizer => return new ContainerMatrix(player, tile)
        }
      }
      case 1 => return new ContainerFrequency(player, player.getCurrentEquippedItem)
      case 2 => return new ContainerItem(player, player.getCurrentEquippedItem)
    }

    return null
  }

  def getClientWorld: World =
  {
    return null
  }

  /**
   * Checks if the player is an operator.
   */
  def isOp(profile: GameProfile): Boolean =
  {
    val theServer: MinecraftServer = FMLCommonHandler.instance.getMinecraftServerInstance()

    if (theServer != null)
    {
      return theServer.getConfigurationManager().func_152596_g(profile)
    }
    return false
  }

  def renderBeam(world: World, position: Vector3, target: Vector3, color: (Float, Float, Float), age: Int)
  {
  }

  def renderHologram(world: World, position: Vector3, color: (Float, Float, Float), age: Int, targetPosition: Vector3)
  {
  }

  def renderHologramMoving(world: World, position: Vector3, color: (Float, Float, Float), age: Int)
  {
  }

  def renderHologramOrbit(world: World, orbitCenter: Vector3, color: (Float, Float, Float), age: Int, maxSpeed: Float)
  {
  }

  def renderHologramOrbit(owner: IEffectController, world: World, orbitCenter: Vector3, position: Vector3, color: (Float, Float, Float), age: Int, maxSpeed: Float)
  {
  }
}
package mffs

import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.network.IGuiHandler
import mffs.container._
import mffs.field.{ContainerForceFieldProjector, TileElectromagnetProjector}
import mffs.mobilize.{ContainerForceManipulator, TileForceMobilizer}
import mffs.render.fx.IEffectController
import mffs.security.{ContainerInterdictionMatrix, ContainerBiometricIdentifier, TileInterdictionMatrix, TileBiometricIdentifier}
import mffs.production._
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import resonant.lib.prefab.ProxyBase

class CommonProxy extends ProxyBase with IGuiHandler
{
  def getServerGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef =
  {
    val tileEntity: TileEntity = world.getBlockTileEntity(x, y, z)
    if (tileEntity != null)
    {
      if (tileEntity.getClass == classOf[TileFortronCapacitor])
      {
        return new ContainerFortronCapacitor(player, tileEntity.asInstanceOf[TileFortronCapacitor])
      }
      else if (tileEntity.getClass == classOf[TileElectromagnetProjector])
      {
        return new ContainerForceFieldProjector(player, tileEntity.asInstanceOf[TileElectromagnetProjector])
      }
      else if (tileEntity.getClass == classOf[TileCoercionDeriver])
      {
        return new ContainerCoercionDeriver(player, tileEntity.asInstanceOf[TileCoercionDeriver])
      }
      else if (tileEntity.getClass == classOf[TileBiometricIdentifier])
      {
        return new ContainerBiometricIdentifier(player, tileEntity.asInstanceOf[TileBiometricIdentifier])
      }
      else if (tileEntity.getClass == classOf[TileInterdictionMatrix])
      {
        return new ContainerInterdictionMatrix(player, tileEntity.asInstanceOf[TileInterdictionMatrix])
      }
      else if (tileEntity.getClass == classOf[TileForceMobilizer])
      {
        return new ContainerForceManipulator(player, tileEntity.asInstanceOf[TileForceMobilizer])
      }
    }
    return null
  }

  def getClientWorld: World =
  {
    return null
  }

  /**
   * Checks if the player is an operator.
   *
   * @param username
   * @author King_Lemming
   */
  def isOp(username: String): Boolean =
  {
    val theServer: MinecraftServer = FMLCommonHandler.instance.getMinecraftServerInstance
    if (theServer != null)
    {
      return theServer.getConfigurationManager.getOps.contains(username.trim.toLowerCase)
    }
    return false
  }

  def renderBeam(world: World, position: Nothing, target: Nothing, red: Float, green: Float, blue: Float, age: Int)
  {
  }

  def renderHologram(world: World, position: Nothing, red: Float, green: Float, blue: Float, age: Int, targetPosition: Nothing)
  {
  }

  def renderHologramMoving(world: World, position: Nothing, red: Float, green: Float, blue: Float, age: Int)
  {
  }

  def renderHologramOrbit(world: World, orbitCenter: Nothing, position: Nothing, red: Float, green: Float, blue: Float, age: Int, maxSpeed: Float)
  {
  }

  def renderHologramOrbit(owner: IEffectController, world: World, orbitCenter: Nothing, position: Nothing, red: Float, green: Float, blue: Float, age: Int, maxSpeed: Float)
  {
  }
}
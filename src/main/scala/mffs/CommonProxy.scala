package mffs

import com.mojang.authlib.GameProfile
import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.network.IGuiHandler
import mffs.field.{ContainerForceFieldProjector, TileElectromagnetProjector}
import mffs.mobilize.{ContainerForceManipulator, TileForceMobilizer}
import mffs.production._
import mffs.render.fx.IEffectController
import mffs.security.{ContainerBiometricIdentifier, ContainerInterdictionMatrix, TileBiometricIdentifier, TileInterdictionMatrix}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.world.World
import resonant.lib.prefab.ProxyBase
import universalelectricity.core.transform.vector.Vector3

class CommonProxy extends ProxyBase with IGuiHandler
{
  override def getServerGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef =
  {
    val tileEntity = world.getTileEntity(x, y, z)

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

  override def getClientWorld: World =
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

  override def renderBeam(world: World, position: Vector3, target: Vector3, red: Float, green: Float, blue: Float, age: Int)
  {
  }

  override def renderHologram(world: World, position: Vector3, red: Float, green: Float, blue: Float, age: Int, targetPosition: Vector3)
  {
  }

  override def renderHologramMoving(world: World, position: Vector3, red: Float, green: Float, blue: Float, age: Int)
  {
  }

  override def renderHologramOrbit(world: World, orbitCenter: Vector3, position: Vector3, red: Float, green: Float, blue: Float, age: Int, maxSpeed: Float)
  {
  }

  override def renderHologramOrbit(owner: IEffectController, world: World, orbitCenter: Vector3, position: Vector3, red: Float, green: Float, blue: Float, age: Int, maxSpeed: Float)
  {
  }
}
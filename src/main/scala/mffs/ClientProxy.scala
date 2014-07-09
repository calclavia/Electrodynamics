package mffs

import com.mojang.authlib.GameProfile
import cpw.mods.fml.client.FMLClientHandler
import mffs.field.{GuiElectromagneticProjector, TileElectromagneticProjector}
import mffs.item.card.RenderIDCard
import mffs.item.gui.GuiFrequency
import mffs.mobilize.{GuiForceMobilizer, TileForceMobilizer}
import mffs.production._
import mffs.render.fx._
import mffs.security.{GuiBiometricIdentifier, TileBiometricIdentifier}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import net.minecraftforge.client.MinecraftForgeClient
import universalelectricity.core.transform.vector.Vector3

class ClientProxy extends CommonProxy
{

  override def init()
  {
    super.init()
    MinecraftForgeClient.registerItemRenderer(ModularForceFieldSystem.Items.cardID, new RenderIDCard())
  }

  override def getClientWorld(): World = FMLClientHandler.instance.getClient.theWorld

  override def getClientGuiElement(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef =
  {
    id match
    {
      case 0 =>
      {
        val tileEntity = world.getTileEntity(x, y, z)

        if (tileEntity != null)
        {
          if (tileEntity.getClass == classOf[TileFortronCapacitor])
          {
            return new GuiFortronCapacitor(player, tileEntity.asInstanceOf[TileFortronCapacitor])
          }
          else if (tileEntity.getClass == classOf[TileElectromagneticProjector])
          {
            return new GuiElectromagneticProjector(player, tileEntity.asInstanceOf[TileElectromagneticProjector])
          }
          else if (tileEntity.getClass == classOf[TileCoercionDeriver])
          {
            return new GuiCoercionDeriver(player, tileEntity.asInstanceOf[TileCoercionDeriver])
          }
          else if (tileEntity.getClass == classOf[TileBiometricIdentifier])
          {
            return new GuiBiometricIdentifier(player, tileEntity.asInstanceOf[TileBiometricIdentifier])
          }
          /* else if (tileEntity.getClass == classOf[TileInterdictionMatrix])
        {
          return new GuiInterdictionMatrix(player, tileEntity.asInstanceOf[TileInterdictionMatrix])
        }*/
          else if (tileEntity.getClass == classOf[TileForceMobilizer])
          {
            return new GuiForceMobilizer(player, tileEntity.asInstanceOf[TileForceMobilizer])
          }
        }
      }
      case 1 => return new GuiFrequency(player, player.getCurrentEquippedItem)
    }

    return null
  }

  override def isOp(profile: GameProfile) = false

  override def renderBeam(world: World, position: Vector3, target: Vector3, red: Float, green: Float, blue: Float, age: Int)
  {
    FMLClientHandler.instance.getClient.effectRenderer.addEffect(new FXFortronBeam(world, position, target, red, green, blue, age))
  }

  override def renderHologram(world: World, position: Vector3, red: Float, green: Float, blue: Float, age: Int, targetPosition: Vector3)
  {
    if (targetPosition != null)
    {
      FMLClientHandler.instance.getClient.effectRenderer.addEffect(new FXHologram(world, position, red, green, blue, age).setTarget(targetPosition))
    }
    else
    {
      FMLClientHandler.instance.getClient.effectRenderer.addEffect(new FXHologram(world, position, red, green, blue, age))
    }
  }

  override def renderHologramOrbit(world: World, orbitCenter: Vector3, position: Vector3, red: Float, green: Float, blue: Float, age: Int, maxSpeed: Float)
  {
    FMLClientHandler.instance.getClient.effectRenderer.addEffect(new FXHologramOrbit(world, orbitCenter, position, red, green, blue, age, maxSpeed))
  }

  override def renderHologramOrbit(controller: IEffectController, world: World, orbitCenter: Vector3, position: Vector3, red: Float, green: Float, blue: Float, age: Int, maxSpeed: Float)
  {
    val fx: FXMFFS = new FXHologramOrbit(world, orbitCenter, position, red, green, blue, age, maxSpeed)
    fx.setController(controller)
    FMLClientHandler.instance.getClient.effectRenderer.addEffect(fx)
  }

  override def renderHologramMoving(world: World, position: Vector3, red: Float, green: Float, blue: Float, age: Int)
  {
    FMLClientHandler.instance.getClient.effectRenderer.addEffect(new FXHologramMoving(world, position, red, green, blue, age))
  }
}
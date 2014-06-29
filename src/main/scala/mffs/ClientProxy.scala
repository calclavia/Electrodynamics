package mffs

import cpw.mods.fml.client.FMLClientHandler
import mffs.gui._
import mffs.render._
import mffs.tile._
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.common.MinecraftForge

class ClientProxy extends CommonProxy
{
  override def preInit()
  {
    super.preInit
    MinecraftForge.EVENT_BUS.register(SoundHandler.INSTANCE)
  }

  override def init()
  {
    super.init()
    MinecraftForgeClient.registerItemRenderer(ModularForceFieldSystem.itemCardID, new RenderIDCard())
  }

  override def getClientWorld(): World = FMLClientHandler.instance.getClient.theWorld

  override def getClientGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef =
  {
    val tileEntity = world.getTileEntity(x, y, z)

    if (tileEntity != null)
    {
      if (tileEntity.getClass eq classOf[TileFortronCapacitor])
      {
        return new GuiFortronCapacitor(player, tileEntity.asInstanceOf[TileFortronCapacitor])
      }
      else if (tileEntity.getClass eq classOf[TileForceFieldProjector])
      {
        return new GuiForceFieldProjector(player, tileEntity.asInstanceOf[TileForceFieldProjector])
      }
      else if (tileEntity.getClass eq classOf[TileCoercionDeriver])
      {
        return new GuiCoercionDeriver(player, tileEntity.asInstanceOf[TileCoercionDeriver])
      }
      else if (tileEntity.getClass eq classOf[TileBiometricIdentifier])
      {
        return new GuiBiometricIdentifier(player, tileEntity.asInstanceOf[TileBiometricIdentifier])
      }
      else if (tileEntity.getClass eq classOf[TileInterdictionMatrix])
      {
        return new GuiInterdictionMatrix(player, tileEntity.asInstanceOf[TileInterdictionMatrix])
      }
      else if (tileEntity.getClass eq classOf[TileForceManipulator])
      {
        return new GuiForceManipulator(player, tileEntity.asInstanceOf[TileForceManipulator])
      }
    }
    return null
  }

  override def isOp(username: String): Boolean =
  {
    return false
  }

  override def renderBeam(world: World, position: Nothing, target: Nothing, red: Float, green: Float, blue: Float, age: Int)
  {
    FMLClientHandler.instance.getClient.effectRenderer.addEffect(new FXFortronBeam(world, position, target, red, green, blue, age))
  }

  override def renderHologram(world: World, position: Nothing, red: Float, green: Float, blue: Float, age: Int, targetPosition: Nothing)
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

  override def renderHologramOrbit(world: World, orbitCenter: Nothing, position: Nothing, red: Float, green: Float, blue: Float, age: Int, maxSpeed: Float)
  {
    FMLClientHandler.instance.getClient.effectRenderer.addEffect(new FXHologramOrbit(world, orbitCenter, position, red, green, blue, age, maxSpeed))
  }

  override def renderHologramOrbit(controller: IEffectController, world: World, orbitCenter: Nothing, position: Nothing, red: Float, green: Float, blue: Float, age: Int, maxSpeed: Float)
  {
    val fx: FXMFFS = new FXHologramOrbit(world, orbitCenter, position, red, green, blue, age, maxSpeed)
    fx.setController(controller)
    FMLClientHandler.instance.getClient.effectRenderer.addEffect(fx)
  }

  override def renderHologramMoving(world: World, position: Nothing, red: Float, green: Float, blue: Float, age: Int)
  {
    FMLClientHandler.instance.getClient.effectRenderer.addEffect(new FXHologramMoving(world, position, red, green, blue, age))
  }
}
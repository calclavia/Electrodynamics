package resonantinduction.electrical

import codechicken.multipart.{TMultiPart, TileMultipart}
import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.client.registry.ClientRegistry
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import resonantinduction.electrical.battery.{RenderBattery, TileBattery}
import resonantinduction.electrical.generator.{TileMotor, RenderMotor}
import resonantinduction.electrical.laser.emitter.{RenderLaserEmitter, TileLaserEmitter}
import resonantinduction.electrical.laser.focus.crystal.{RenderFocusCrystal, TileFocusCrystal}
import resonantinduction.electrical.laser.focus.mirror.{RenderMirror, TileMirror}
import resonantinduction.electrical.laser.fx.{EntityBlockParticleFX, EntityLaserFX, EntityScorchFX}
import resonantinduction.electrical.laser.receiver.{RenderLaserReceiver, TileLaserReceiver}
import resonantinduction.electrical.multimeter.{GuiMultimeter, PartMultimeter}
import resonantinduction.electrical.render.FXElectricBolt
import resonantinduction.electrical.tesla.{RenderTesla, TileTesla}
import universalelectricity.core.transform.vector.Vector3

/** @author Calclavia */
@SideOnly(Side.CLIENT) class ClientProxy extends CommonProxy {
  override def preInit
  {

  }

  override def init {
    //RenderingRegistry.registerBlockHandler(new BlockRenderingHandler.type)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileMotor], new RenderMotor)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileTesla], new RenderTesla)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileBattery], new RenderBattery)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileLaserEmitter], RenderLaserEmitter)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileLaserReceiver], RenderLaserReceiver)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileMirror], RenderMirror)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileFocusCrystal], RenderFocusCrystal)
  }

  override def postInit {
  }

  override def getClientGuiElement(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = {
    val tileEntity: TileEntity = world.getTileEntity(x, y, z)
    if (tileEntity.isInstanceOf[TileMultipart]) {
      val part: TMultiPart = (tileEntity.asInstanceOf[TileMultipart]).partMap(id)
      if (part.isInstanceOf[PartMultimeter]) {
        return new GuiMultimeter(player.inventory, part.asInstanceOf[PartMultimeter])
      }
    }
    return null
  }

  override def renderElectricShock(world: World, start: Vector3, target: Vector3, r: Float, g: Float, b: Float, split: Boolean) {
    if (world.isRemote) {
      FMLClientHandler.instance.getClient.effectRenderer.addEffect(new FXElectricBolt(world, start, target, split).setColor(r, g, b))
    }
  }



  override def renderScorch(world: World, position: Vector3, side: Int)
  {
    if (FMLClientHandler.instance.getClient.gameSettings.particleSetting != 2)
      FMLClientHandler.instance().getClient.effectRenderer.addEffect(new EntityScorchFX(world, position, side))
  }

  override def renderBlockParticle(world: World, position: Vector3, block: Block, side: Int)
  {
    if (FMLClientHandler.instance.getClient.gameSettings.particleSetting != 2)
      FMLClientHandler.instance.getClient.effectRenderer.addEffect(new EntityBlockParticleFX(world, position.x, position.y, position.z, 0, 0, 0, block, side))
  }

  override def renderLaser(world: World, start: Vector3, end: Vector3, color: Vector3, enegy: Double)
  {
    FMLClientHandler.instance().getClient.effectRenderer.addEffect(new EntityLaserFX(world, start, end, color, enegy))
  }
}
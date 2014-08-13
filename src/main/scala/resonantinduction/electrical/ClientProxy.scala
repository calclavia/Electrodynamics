package resonantinduction.electrical

import cpw.mods.fml.client.registry.RenderingRegistry
import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.client.model.AdvancedModelLoader
import resonantinduction.electrical.battery.RenderBattery
import resonantinduction.electrical.battery.TileBattery
import resonantinduction.electrical.laser.BlockRenderingHandler
import resonantinduction.electrical.laser.emitter.RenderLaserEmitter
import resonantinduction.electrical.laser.focus.crystal.RenderFocusCrystal
import resonantinduction.electrical.laser.focus.mirror.RenderMirror
import resonantinduction.electrical.laser.fx.{EntityLaserFX, EntityBlockParticleFX, EntityScorchFX}
import resonantinduction.electrical.levitator.RenderLevitator
import resonantinduction.electrical.multimeter.GuiMultimeter
import resonantinduction.electrical.multimeter.PartMultimeter
import resonantinduction.electrical.multimeter.RenderMultimeter
import resonantinduction.electrical.render.FXElectricBolt
import resonantinduction.electrical.tesla.RenderTesla
import resonantinduction.electrical.tesla.TileTesla
import resonantinduction.electrical.transformer.RenderTransformer
import resonantinduction.atomic.gate.RenderQuantumGlyph
import universalelectricity.core.transform.vector.Vector3
import codechicken.multipart.TMultiPart
import codechicken.multipart.TileMultipart
import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.client.registry.ClientRegistry
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly

/** @author Calclavia */
@SideOnly(Side.CLIENT) class ClientProxy extends CommonProxy {
  override def preInit {
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileTesla], new RenderTesla)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileBattery], new RenderBattery)
  }

  override def init {
    RenderingRegistry.registerBlockHandler(new BlockRenderingHandler.type)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf(TileLaserEmitter), RenderLaserEmitter)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf(TileMirror), RenderMirror)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf(TileFocusCrystal), RenderFocusCrystal)
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
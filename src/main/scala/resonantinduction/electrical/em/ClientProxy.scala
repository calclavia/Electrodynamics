package resonantinduction.electrical.em

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.client.registry.{ClientRegistry, RenderingRegistry}
import net.minecraft.block.Block
import net.minecraft.world.World
import net.minecraftforge.client.model.AdvancedModelLoader
import resonantinduction.electrical.em.laser.BlockRenderingHandler
import resonantinduction.electrical.em.laser.emitter.{RenderLaserEmitter, TileLaserEmitter}
import resonantinduction.electrical.em.laser.focus.crystal.{RenderFocusCrystal, TileFocusCrystal}
import resonantinduction.electrical.em.laser.focus.mirror.{RenderMirror, TileMirror}
import resonantinduction.electrical.em.laser.fx.{EntityBlockParticleFX, EntityLaserFX, EntityScorchFX}

/**
 * @author Calclavia
 */
class ClientProxy extends CommonProxy
{
  override def init()
  {
    super.init()
    AdvancedModelLoader.registerModelHandler(new FixedTechneModelLoader())

    RenderingRegistry.registerBlockHandler(BlockRenderingHandler)

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileLaserEmitter], RenderLaserEmitter)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileMirror], RenderMirror)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileFocusCrystal], RenderFocusCrystal)
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

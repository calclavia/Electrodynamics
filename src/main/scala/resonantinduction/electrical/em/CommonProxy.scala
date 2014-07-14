package resonantinduction.electrical.em

import net.minecraft.world.World
import cpw.mods.fml.common.registry.GameRegistry
import resonantinduction.electrical.em.laser.emitter.{TileLaserReceiver, TileLaserEmitter}
import resonantinduction.electrical.em.laser.focus.mirror.TileMirror
import net.minecraft.block.Block
import resonantinduction.electrical.em.laser.focus.crystal.TileFocusCrystal

/**
 * @author Calclavia
 */
class CommonProxy
{
  def init()
  {
    GameRegistry.registerTileEntity(classOf[TileLaserEmitter], "RILaserEmitter")
    GameRegistry.registerTileEntity(classOf[TileLaserReceiver], "RILaserReceiver")
    GameRegistry.registerTileEntity(classOf[TileMirror], "RIMirror")
    GameRegistry.registerTileEntity(classOf[TileFocusCrystal], "RIFocusCrystal")
  }

  def renderBlockParticle(world: World, position: Vector3, block: Block, side: Int)
  {

  }

  def renderLaser(world: World, start: Vector3, end: Vector3, color: Vector3, energy: Double)
  {

  }

  def renderScorch(world: World, position: Vector3, side: Int)
  {

  }
}

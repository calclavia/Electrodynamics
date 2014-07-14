package resonantinduction.electrical.em.laser

import cpw.mods.fml.client.registry.{RenderingRegistry, ISimpleBlockRenderingHandler}
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.world.IBlockAccess
import net.minecraft.block.Block
import resonantinduction.electrical.em.laser.focus.mirror.{RenderMirror, BlockMirror}
import resonantinduction.electrical.em.laser.emitter.{RenderLaserReceiver, RenderLaserEmitter, BlockLaserEmitter}
import resonantinduction.electrical.em.laser.receiver.BlockLaserReceiver
import cpw.mods.fml.relauncher.{Side, SideOnly}
import resonantinduction.electrical.em.laser.focus.crystal.{BlockFocusCrystal, RenderFocusCrystal}

/**
 * @author Calclavia
 */
@SideOnly(Side.CLIENT)
object BlockRenderingHandler extends ISimpleBlockRenderingHandler
{
  val renderID = RenderingRegistry.getNextAvailableRenderId

  def renderInventoryBlock(block: Block, metadata: Int, modelId: Int, renderer: RenderBlocks)
  {
    if (block.isInstanceOf[BlockLaserEmitter])
    {
      RenderLaserEmitter.renderItem()
    }
    else if (block.isInstanceOf[BlockLaserReceiver])
    {
      RenderLaserReceiver.renderItem()
    }
    else if (block.isInstanceOf[BlockFocusCrystal])
    {
      RenderFocusCrystal.renderItem()
    }
    else if (block.isInstanceOf[BlockMirror])
    {
      RenderMirror.renderItem()
    }
  }

  def renderWorldBlock(world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, modelId: Int, renderer: RenderBlocks) = false

  def shouldRender3DInInventory(modelId: Int) = true

  def getRenderId = renderID
}

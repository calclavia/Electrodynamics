package resonantinduction.electrical.laser

import cpw.mods.fml.client.registry.{ISimpleBlockRenderingHandler, RenderingRegistry}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.Block
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.world.IBlockAccess
import resonantinduction.electrical.laser.emitter.{BlockLaserEmitter, RenderLaserEmitter}
import resonantinduction.electrical.laser.focus.crystal.{BlockFocusCrystal, RenderFocusCrystal}
import resonantinduction.electrical.laser.focus.mirror.{BlockMirror, RenderMirror}
import resonantinduction.electrical.laser.receiver.{BlockLaserReceiver, RenderLaserReceiver}

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

package resonantinduction.core.grid.fluid.distribution

import net.minecraft.block.material.Material
import resonantinduction.core.grid.fluid.TileFluidNodeProvider

/**
 * A prefab class for tiles that use the fluid network.
 *
 * @author DarkGuardsman
 */
abstract class TileFluidDistribution(material: Material) extends TileFluidNodeProvider(material) with TFluidDistributor with TFluidForwarder
{
  tankNode.onChange = () => sendRenderUpdate

  override def start()
  {
    super.start()
    tankNode.reconstruct()
  }

  override def invalidate()
  {
    tankNode.deconstruct()
    super.invalidate()
  }

  /**
   * Network used to link all parts together
   */
  protected var tankNode: TankNode = null

  override def getForwardTank = tankNode
}
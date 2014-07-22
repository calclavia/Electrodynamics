package resonantinduction.archaic

import resonantinduction.archaic.fluid.tank.TileTank
import resonant.lib.render.item.GlobalItemRenderer
import resonantinduction.archaic.fluid.tank.TileTank
import resonantinduction.archaic.fluid.tank.TileTank

class ClientProxy extends CommonProxy
{
  override def preInit
  {
    GlobalItemRenderer.register(ArchaicBlocks.blockTank, TileTank.ItemRenderer.instance)
  }
}
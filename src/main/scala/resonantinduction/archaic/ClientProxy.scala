package resonantinduction.archaic

import cpw.mods.fml.client.registry.ClientRegistry
import resonantinduction.archaic.firebox.{RenderHotPlate, TileHotPlate}
import resonantinduction.archaic.process.{RenderMillstone, TileMillstone, RenderCastingMold, TileCastingMold}

class ClientProxy extends CommonProxy
{
  override def init
  {
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileCastingMold], new RenderCastingMold)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileMillstone], new RenderMillstone)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileHotPlate], new RenderHotPlate)
  }
}
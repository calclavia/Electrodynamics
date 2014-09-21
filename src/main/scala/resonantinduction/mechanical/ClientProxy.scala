package resonantinduction.mechanical

import cpw.mods.fml.client.registry.ClientRegistry
import net.minecraft.item.Item
import resonant.content.wrapper.ItemRenderHandler
import resonantinduction.mechanical.fluid.pipe.RenderPipe
import resonantinduction.mechanical.fluid.transport.RenderPump
import resonantinduction.mechanical.fluid.transport.TilePump
import resonantinduction.mechanical.mech.gear.RenderGear
import resonantinduction.mechanical.mech.gearshaft.RenderGearShaft
import resonantinduction.mechanical.mech.process.crusher.RenderMechanicalPiston
import resonantinduction.mechanical.mech.process.crusher.TileMechanicalPiston
import resonantinduction.mechanical.mech.process.grinder.RenderGrindingWheel
import resonantinduction.mechanical.mech.process.grinder.TileGrindingWheel
import resonantinduction.mechanical.mech.process.mixer.RenderMixer
import resonantinduction.mechanical.mech.process.mixer.TileMixer
import resonantinduction.mechanical.mech.turbine._

class ClientProxy extends CommonProxy
{
  override def init()
  {
    ItemRenderHandler.register(Mechanical.itemGear, RenderGear.INSTANCE)
    ItemRenderHandler.register(Mechanical.itemGearShaft, RenderGearShaft.INSTANCE)
    ItemRenderHandler.register(Mechanical.itemPipe, RenderPipe)
    ItemRenderHandler.register(Item.getItemFromBlock(Mechanical.blockWaterTurbine), new RenderWaterTurbine)
    ItemRenderHandler.register(Item.getItemFromBlock(Mechanical.blockWindTurbine), new RenderWindTurbine)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileMechanicalPiston], new RenderMechanicalPiston)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileGrindingWheel], new RenderGrindingWheel)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileMixer], new RenderMixer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileWaterTurbine], new RenderWaterTurbine)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileWindTurbine], new RenderWindTurbine)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileElectricTurbine], new RenderElectricTurbine)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TilePump], new RenderPump)
  }
}
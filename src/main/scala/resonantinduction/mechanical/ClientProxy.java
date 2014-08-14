package resonantinduction.mechanical;

import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.item.Item;
import resonant.content.wrapper.ItemRenderHandler;
import resonantinduction.mechanical.fluid.pipe.RenderPipe;
import resonantinduction.mechanical.gear.RenderGear;
import resonantinduction.mechanical.gearshaft.RenderGearShaft;
import resonantinduction.mechanical.process.crusher.RenderMechanicalPiston;
import resonantinduction.mechanical.process.crusher.TileMechanicalPiston;
import resonantinduction.mechanical.process.grinder.RenderGrindingWheel;
import resonantinduction.mechanical.process.grinder.TileGrindingWheel;
import resonantinduction.mechanical.process.mixer.RenderMixer;
import resonantinduction.mechanical.process.mixer.TileMixer;
import resonantinduction.mechanical.turbine.*;

public class ClientProxy extends CommonProxy
{
    @Override
    public void init()
    {
        ItemRenderHandler.register(Mechanical.itemGear, RenderGear.INSTANCE);
        ItemRenderHandler.register(Mechanical.itemGearShaft, RenderGearShaft.INSTANCE);
        ItemRenderHandler.register(Mechanical.itemPipe, RenderPipe.INSTANCE);

        ItemRenderHandler.register(Item.getItemFromBlock(Mechanical.blockWaterTurbine), new RenderWaterTurbine());
        ItemRenderHandler.register(Item.getItemFromBlock(Mechanical.blockWindTurbine), new RenderWindTurbine());

        ClientRegistry.bindTileEntitySpecialRenderer(TileMechanicalPiston.class, new RenderMechanicalPiston());
        ClientRegistry.bindTileEntitySpecialRenderer(TileGrindingWheel.class, new RenderGrindingWheel());
        ClientRegistry.bindTileEntitySpecialRenderer(TileMixer.class, new RenderMixer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileWaterTurbine.class, new RenderWaterTurbine());
        ClientRegistry.bindTileEntitySpecialRenderer(TileWindTurbine.class, new RenderWindTurbine());
        ClientRegistry.bindTileEntitySpecialRenderer(TileElectricTurbine.class, new RenderElectricTurbine());
    }
}

package resonantinduction.mechanical;

import net.minecraftforge.client.MinecraftForgeClient;
import resonantinduction.mechanical.fluid.pipe.ItemPipeRenderer;
import resonantinduction.mechanical.fluid.tank.ItemTankRenderer;
import resonantinduction.mechanical.gear.RenderGear;
import resonantinduction.mechanical.gear.RenderGearShaft;
import calclavia.lib.render.item.GlobalItemRenderer;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit()
	{
		MinecraftForgeClient.registerItemRenderer(Mechanical.itemGear.itemID, GlobalItemRenderer.INSTANCE);
		GlobalItemRenderer.register(Mechanical.itemGear.itemID, RenderGear.INSTANCE);
		MinecraftForgeClient.registerItemRenderer(Mechanical.itemGearShaft.itemID, GlobalItemRenderer.INSTANCE);
		GlobalItemRenderer.register(Mechanical.itemGearShaft.itemID, RenderGearShaft.INSTANCE);
	}

	@Override
	public void init()
	{
		MinecraftForgeClient.registerItemRenderer(Mechanical.blockTank.blockID, new ItemTankRenderer());
		MinecraftForgeClient.registerItemRenderer(Mechanical.itemPipe.itemID, new ItemPipeRenderer());
		;
	}
}

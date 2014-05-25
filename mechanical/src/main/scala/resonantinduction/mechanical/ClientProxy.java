package resonantinduction.mechanical;

import resonant.lib.render.item.GlobalItemRenderer;
import resonantinduction.mechanical.fluid.pipe.RenderPipe;
import resonantinduction.mechanical.gear.RenderGear;
import resonantinduction.mechanical.gearshaft.RenderGearShaft;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit()
	{
		GlobalItemRenderer.register(Mechanical.itemGear.itemID, RenderGear.INSTANCE);
		GlobalItemRenderer.register(Mechanical.itemGearShaft.itemID, RenderGearShaft.INSTANCE);
		GlobalItemRenderer.register(Mechanical.itemPipe.itemID, RenderPipe.INSTANCE);
	}
}

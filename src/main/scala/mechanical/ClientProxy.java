package mechanical;

import mechanical.fluid.pipe.RenderPipe;
import resonant.lib.render.item.GlobalItemRenderer;
import mechanical.gear.RenderGear;
import mechanical.gearshaft.RenderGearShaft;

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

package resonantinduction.mechanical;

import resonant.lib.render.item.GlobalItemRenderer;
import resonantinduction.mechanical.energy.gear.RenderGear;
import resonantinduction.mechanical.energy.gearshaft.RenderGearShaft;
import resonantinduction.mechanical.fluid.pipe.RenderPipe;

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

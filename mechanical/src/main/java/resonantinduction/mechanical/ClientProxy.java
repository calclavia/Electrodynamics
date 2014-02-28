package resonantinduction.mechanical;

import resonantinduction.mechanical.energy.gear.RenderGear;
import resonantinduction.mechanical.energy.gear.RenderGearShaft;
import resonantinduction.mechanical.fluid.pipe.RenderPipe;
import calclavia.lib.render.item.GlobalItemRenderer;

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

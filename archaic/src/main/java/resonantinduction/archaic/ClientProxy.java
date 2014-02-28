package resonantinduction.archaic;

import resonantinduction.archaic.fluid.tank.RenderTank;
import calclavia.lib.render.item.GlobalItemRenderer;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit()
	{
		GlobalItemRenderer.register(Archaic.blockTank.blockID, RenderTank.INSTANCE);
	}
}

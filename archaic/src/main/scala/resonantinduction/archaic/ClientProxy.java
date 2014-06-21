package resonantinduction.archaic;

import resonant.lib.render.item.GlobalItemRenderer;
import resonantinduction.archaic.fluid.tank.TileTank;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit()
	{
		GlobalItemRenderer.register(Archaic.blockTank.blockID, TileTank.ItemRenderer.instance);
	}
}

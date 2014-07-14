package resonantinduction.mechanical;

import resonantinduction.mechanical.render.MechanicalBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{
	public void preInit()
	{
		RenderingRegistry.registerBlockHandler(MechanicalBlockRenderingHandler.INSTANCE);
	}
}

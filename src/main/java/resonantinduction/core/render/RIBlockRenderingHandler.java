package resonantinduction.core.render;

import calclavia.lib.render.block.BlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RIBlockRenderingHandler extends BlockRenderingHandler
{
	public static final RIBlockRenderingHandler INSTANCE = new RIBlockRenderingHandler();
	public static final int ID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public int getRenderId()
	{
		return ID;
	}
}

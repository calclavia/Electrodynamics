package resonantinduction.core;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import resonantinduction.core.render.RIBlockRenderingHandler;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit()
	{
		MinecraftForge.EVENT_BUS.register(SoundHandler.INSTANCE);
		RenderingRegistry.registerBlockHandler(RIBlockRenderingHandler.INSTANCE);
	}

	@Override
	public void postInit()
	{
	}

	@Override
	public boolean isPaused()
	{
		if (FMLClientHandler.instance().getClient().isSingleplayer() && !FMLClientHandler.instance().getClient().getIntegratedServer().getPublic())
		{
			GuiScreen screen = FMLClientHandler.instance().getClient().currentScreen;

			if (screen != null)
			{
				if (screen.doesGuiPauseGame())
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean isGraphicsFancy()
	{
		return FMLClientHandler.instance().getClient().gameSettings.fancyGraphics;
	}
}

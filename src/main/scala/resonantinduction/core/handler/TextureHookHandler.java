package resonantinduction.core.handler;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.TextureStitchEvent;
import resonant.lib.render.RenderUtility;
import resonantinduction.core.Reference;

/**
 * @author Calclavia
 */
public class TextureHookHandler
{
	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void preTextureHook(TextureStitchEvent.Pre event)
	{
		if (event.map.getTextureType() == 0)
		{
			RenderUtility.registerIcon(Reference.prefix() + "glyph_0", event.map);
			RenderUtility.registerIcon(Reference.prefix() + "glyph_1", event.map);
			RenderUtility.registerIcon(Reference.prefix() + "glyph_2", event.map);
			RenderUtility.registerIcon(Reference.prefix() + "glyph_3", event.map);

			RenderUtility.registerIcon(Reference.prefix() + "mixture_flow", event.map);
			RenderUtility.registerIcon(Reference.prefix() + "molten_flow", event.map);
			RenderUtility.registerIcon(Reference.prefix() + "multimeter_screen", event.map);
			RenderUtility.registerIcon(Reference.prefix() + "tankEdge", event.map);
		}
	}
}

package resonantinduction.core.handler;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.fluids.Fluid;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import calclavia.lib.render.RenderUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
public class TextureHookHandler
{
	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void preTextureHook(TextureStitchEvent.Pre event)
	{
		if (event.map.textureType == 0)
		{
			RenderUtility.registerIcon(Reference.PREFIX + "mixture_flow", event.map);
			RenderUtility.registerIcon(Reference.PREFIX + "molten_flow", event.map);
			RenderUtility.registerIcon(Reference.PREFIX + "multimeter_screen", event.map);
		}
	}

	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Post event)
	{
		for (Fluid f : ResonantInduction.fluidMixtures)
			f.setIcons(RenderUtility.loadedIconMap.get(Reference.PREFIX + "mixture_flow"));
		for (Fluid f : ResonantInduction.fluidMaterials)
			f.setIcons(RenderUtility.loadedIconMap.get(Reference.PREFIX + "molten_flow"));
	}
}

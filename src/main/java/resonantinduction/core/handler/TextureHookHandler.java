package resonantinduction.core.handler;

import java.util.HashMap;

import net.minecraft.util.Icon;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.fluids.Fluid;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
public class TextureHookHandler
{
	public static final HashMap<String, Icon> loadedIconMap = new HashMap<String, Icon>();

	public void registerIcon(String name, TextureStitchEvent.Pre event)
	{
		loadedIconMap.put(name, event.map.registerIcon(name));
	}

	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void preTextureHook(TextureStitchEvent.Pre event)
	{
		if (event.map.textureType == 0)
		{
			registerIcon(Reference.PREFIX + "mixture_flow", event);
			registerIcon(Reference.PREFIX + "molten_flow", event);
			registerIcon(Reference.PREFIX + "multimeter_screen", event);
		}
	}

	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Post event)
	{
		for (Fluid f : ResonantInduction.fluidMixtures)
			f.setIcons(loadedIconMap.get(Reference.PREFIX + "mixture_flow"));
		for (Fluid f : ResonantInduction.fluidMaterials)
			f.setIcons(loadedIconMap.get(Reference.PREFIX + "molten_flow"));
	}
}

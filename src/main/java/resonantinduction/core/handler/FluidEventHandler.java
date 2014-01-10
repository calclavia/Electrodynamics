package resonantinduction.core.handler;

import java.util.HashMap;

import net.minecraft.util.Icon;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.ForgeSubscribe;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
public class FluidEventHandler
{
	public static final HashMap<String, Icon> fluidIconMap = new HashMap<String, Icon>();

	public void registerIcon(String name, TextureStitchEvent.Pre event)
	{
		fluidIconMap.put(name, event.map.registerIcon(name));
	}

	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void preTextureHook(TextureStitchEvent.Pre event)
	{
		if (event.map.textureType == 0)
		{
			registerIcon(Reference.PREFIX + "mixture", event);
		}
	}

	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Post event)
	{
		ResonantInduction.MIXTURE.setIcons(fluidIconMap.get(Reference.PREFIX + "mixture"));
	}
}

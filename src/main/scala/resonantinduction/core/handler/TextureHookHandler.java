package resonantinduction.core.handler;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.fluids.BlockFluidFinite;
import resonant.lib.render.RenderUtility;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.fluid.FluidColored;
import resonantinduction.core.resource.ResourceGenerator;
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
			RenderUtility.registerIcon(Reference.PREFIX + "glyph_0", event.map);
			RenderUtility.registerIcon(Reference.PREFIX + "glyph_1", event.map);
			RenderUtility.registerIcon(Reference.PREFIX + "glyph_2", event.map);
			RenderUtility.registerIcon(Reference.PREFIX + "glyph_3", event.map);

			RenderUtility.registerIcon(Reference.PREFIX + "mixture_flow", event.map);
			RenderUtility.registerIcon(Reference.PREFIX + "molten_flow", event.map);
			RenderUtility.registerIcon(Reference.PREFIX + "multimeter_screen", event.map);
			RenderUtility.registerIcon(Reference.PREFIX + "tankEdge", event.map);
		}
	}

	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void postTextureHook(TextureStitchEvent.Post event)
	{
		for (BlockFluidFinite block : ResonantInduction.blockMixtureFluids.values())
		{
			block.getFluid().setIcons(RenderUtility.getIcon(Reference.PREFIX + "mixture_flow"));
			((FluidColored) block.getFluid()).setColor(ResourceGenerator.getColor(ResourceGenerator.mixtureToMaterial(block.getFluid().getName())));
		}

		for (BlockFluidFinite block : ResonantInduction.blockMoltenFluid.values())
		{
			block.getFluid().setIcons(RenderUtility.getIcon(Reference.PREFIX + "molten_flow"));
			((FluidColored) block.getFluid()).setColor(ResourceGenerator.getColor(ResourceGenerator.moltenToMaterial(block.getFluid().getName())));
		}
	}
}

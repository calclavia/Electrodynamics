package resonantinduction.mechanical.fluid.pipe;

import java.util.HashMap;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import calclavia.lib.render.RenderUtility;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPipe
{
	public static final RenderPipe INSTANCE = new RenderPipe();

	public static ModelPipe MODEL_PIPE = new ModelPipe();
	public static ModelOpenTrough MODEL_TROUGH_PIPE = new ModelOpenTrough();
	private static HashMap<EnumPipeMaterial, ResourceLocation> TEXTURES = new HashMap<EnumPipeMaterial, ResourceLocation>();
	public static ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "pipe.png");

	public void render(PartPipe part, double x, double y, double z, float f)
	{
		EnumPipeMaterial material = part.getMaterial();
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(getTexture(material));
		MODEL_PIPE.render(part.getAllCurrentConnections());
		GL11.glPopMatrix();
	}

	public static ResourceLocation getTexture(EnumPipeMaterial material)
	{
		/*
		 * if (material != null)
		 * {
		 * if (!TEXTURES.containsKey(material))
		 * {
		 * TEXTURES.put(material, new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH +
		 * "pipe/" + material.matName + ".png"));
		 * }
		 * return TEXTURES.get(material);
		 * }
		 */

		return TEXTURE;
	}

	public static void render(int meta, byte sides)
	{
		if (meta < EnumPipeMaterial.values().length)
		{
			RenderUtility.enableBlending();
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(getTexture(EnumPipeMaterial.values()[meta]));
			MODEL_PIPE.render(sides);
			RenderUtility.disableBlending();
		}
	}
}
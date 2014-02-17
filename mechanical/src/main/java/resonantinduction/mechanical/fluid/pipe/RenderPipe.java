package resonantinduction.mechanical.fluid.pipe;

import net.minecraft.util.ResourceLocation;

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
	public static ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "pipe.png");

	public void render(PartPipe part, double x, double y, double z, float f)
	{
		EnumPipeMaterial material = part.getMaterial();
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		render(0, part.getAllCurrentConnections());
		GL11.glPopMatrix();
	}

	public static ResourceLocation getTexture(EnumPipeMaterial material)
	{
		return TEXTURE;
	}

	public static void render(int meta, byte sides)
	{
		if (meta < EnumPipeMaterial.values().length)
		{
			RenderUtility.enableBlending();
			RenderUtility.bind((getTexture(EnumPipeMaterial.values()[meta])));
			MODEL_PIPE.render(sides);
			RenderUtility.disableBlending();
		}
	}
}
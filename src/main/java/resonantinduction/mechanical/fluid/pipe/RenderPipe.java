package resonantinduction.mechanical.fluid.pipe;

import java.util.HashMap;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.mechanical.fluid.prefab.TileFluidNetwork;
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
	public static ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "pipe/iron.png");

	public void render(PartPipe part, double x, double y, double z, float f)
	{
		EnumPipeMaterial material = part.getMaterial();
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(getTexture(material));
		render(material, part.getAllCurrentConnections());
		GL11.glPopMatrix();
	}

	public static ResourceLocation getTexture(EnumPipeMaterial material)
	{
		if (material != null)
		{
			if (!TEXTURES.containsKey(material))
			{
				TEXTURES.put(material, new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "pipe/" + material.matName + ".png"));
			}

			return TEXTURES.get(material);
		}
		return TEXTURE;
	}

	public static void render(EnumPipeMaterial mat, byte side)
	{
		if (TileFluidNetwork.canRenderSide(side, ForgeDirection.DOWN))
		{
			MODEL_PIPE.renderBottom();
		}
		if (TileFluidNetwork.canRenderSide(side, ForgeDirection.UP))
		{
			MODEL_PIPE.renderTop();
		}
		if (TileFluidNetwork.canRenderSide(side, ForgeDirection.NORTH))
		{
			MODEL_PIPE.renderBack();
		}
		if (TileFluidNetwork.canRenderSide(side, ForgeDirection.SOUTH))
		{
			MODEL_PIPE.renderFront();
		}
		if (TileFluidNetwork.canRenderSide(side, ForgeDirection.WEST))
		{
			MODEL_PIPE.renderLeft();
		}
		if (TileFluidNetwork.canRenderSide(side, ForgeDirection.EAST))
		{
			MODEL_PIPE.renderRight();
		}

		MODEL_PIPE.renderMiddle();
	}

	public static void render(int meta, byte sides)
	{
		if (meta < EnumPipeMaterial.values().length)
		{
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(getTexture(EnumPipeMaterial.values()[meta]));
			render(EnumPipeMaterial.values()[meta], sides);
		}
	}
}
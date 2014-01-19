package resonantinduction.mechanical.fluid.pipe;

import java.util.HashMap;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.mechanical.fluid.prefab.TileFluidNetwork;

import com.builtbroken.common.Pair;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPipe
{
	public static final RenderPipe INSTANCE = new RenderPipe();

	public static ModelPipe MODEL_PIPE = new ModelPipe();
	public static ModelOpenTrough MODEL_TROUGH_PIPE = new ModelOpenTrough();
	private static HashMap<Pair<EnumPipeMaterial, Integer>, ResourceLocation> TEXTURES = new HashMap<Pair<EnumPipeMaterial, Integer>, ResourceLocation>();
	public static ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "pipe/iron.png");

	public void render(PartPipe part, double x, double y, double z, float f)
	{
		EnumPipeMaterial material = part.getMaterial();
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(getTexture(material, 0));
		render(material, part.getMaterialID(), part.getAllCurrentConnections());
		GL11.glPopMatrix();
	}

	public static ResourceLocation getTexture(EnumPipeMaterial mat, int pipeID)
	{
		if (mat != null)
		{
			Pair<EnumPipeMaterial, Integer> index = new Pair<EnumPipeMaterial, Integer>(mat, pipeID);

			if (!TEXTURES.containsKey(index))
			{
				String pipeName = "";
				if (EnumPipeType.get(pipeID) != null)
				{
					pipeName = EnumPipeType.get(pipeID).getName(pipeID);
				}
				TEXTURES.put(index, new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "pipe/" + mat.matName + ".png"));
			}
			return TEXTURES.get(index);
		}
		return TEXTURE;
	}

	public static ResourceLocation getTexture(int meta)
	{
		return getTexture(EnumPipeMaterial.getFromItemMeta(meta), EnumPipeMaterial.getType(meta));
	}

	public static void render(EnumPipeMaterial mat, int pipeID, byte side)
	{
		if (mat == EnumPipeMaterial.WOOD)
		{
			MODEL_TROUGH_PIPE.render(side, false);
		}
		else if (mat == EnumPipeMaterial.STONE)
		{
			MODEL_TROUGH_PIPE.render(side, true);
		}
		else
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
	}

	public static void render(int meta, byte sides)
	{
		render(EnumPipeMaterial.getFromItemMeta(meta), EnumPipeMaterial.getType(meta), sides);
	}

}
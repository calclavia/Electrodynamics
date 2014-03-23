package resonantinduction.mechanical.logistic.belt;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.core.render.RenderImprintable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderManipulator extends RenderImprintable
{
	public static final ModelManipulator MODEL = new ModelManipulator();
	public static final ResourceLocation TEXTURE_INPUT = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "manipulator1.png");
	public static final ResourceLocation TEXTURE_OUTPUT = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "manipulator2.png");

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float var8)
	{
		TileManipulator tile = (TileManipulator) tileEntity;

		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GL11.glRotatef(180f, 0f, 0f, 1f);

		if (tile.isOutput())
		{
			bindTexture(TEXTURE_INPUT);
		}
		else
		{
			bindTexture(TEXTURE_OUTPUT);
		}

		if (tile.worldObj != null)
		{
			int face = tile.getDirection().ordinal();

			if (face == 2)
			{
				GL11.glRotatef(0f, 0f, 1f, 0f);
			}
			else if (face == 3)
			{
				GL11.glRotatef(180f, 0f, 1f, 0f);
			}
			else if (face == 4)
			{
				GL11.glRotatef(270f, 0f, 1f, 0f);
			}
			else if (face == 5)
			{
				GL11.glRotatef(90f, 0f, 1f, 0f);
			}
		}

		MODEL.render(0.0625F, true, 0);

		GL11.glPopMatrix();

	}

}
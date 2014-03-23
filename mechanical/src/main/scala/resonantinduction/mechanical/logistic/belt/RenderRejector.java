package resonantinduction.mechanical.logistic.belt;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.core.render.RenderImprintable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderRejector extends RenderImprintable
{
	public static final ModelRejectorPiston MODEL = new ModelRejectorPiston();
	public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "rejector.png");

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float var8)
	{
		TileRejector tile = (TileRejector) tileEntity;
		boolean fire = tile.firePiston;
		int face = tile.getDirection().ordinal();
		int pos = 0;

		if (fire)
		{
			pos = 8;
		}
		bindTexture(TEXTURE);
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		if (face == 2)
		{
			GL11.glRotatef(180f, 0f, 1f, 0f);
		}
		if (face == 3)
		{
			GL11.glRotatef(0f, 0f, 1f, 0f);
		}
		else if (face == 4)
		{
			GL11.glRotatef(90f, 0f, 1f, 0f);
		}
		else if (face == 5)
		{
			GL11.glRotatef(270f, 0f, 1f, 0f);
		}
		MODEL.render(0.0625F);
		MODEL.renderPiston(0.0625F, pos);
		GL11.glPopMatrix();
	}
}
package resonantinduction.mechanical.fluid.pump;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import resonantinduction.old.client.model.ModelPump;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPump extends TileEntitySpecialRenderer
{
	public static final ModelPump MODEL = new ModelPump();
	public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "pump.png");

	@Override
	public void renderTileEntityAt(TileEntity t, double d, double d1, double d2, float f)
	{
		int meta = t.worldObj.getBlockMetadata(t.xCoord, t.yCoord, t.zCoord);

		bindTexture(TEXTURE);
		GL11.glPushMatrix();
		GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		switch (meta)
		{
			case 2:
				GL11.glRotatef(0f, 0f, 1f, 0f);
				break;
			case 3:
				GL11.glRotatef(90f, 0f, 1f, 0f);
				break;
			case 0:
				GL11.glRotatef(180f, 0f, 1f, 0f);
				break;
			case 1:
				GL11.glRotatef(270f, 0f, 1f, 0f);
				break;
		}
		MODEL.render(0.0625F);

		if (t instanceof TilePump)
		{
			MODEL.renderMotion(0.0625F, ((TilePump) t).rotation);
		}

		GL11.glPopMatrix();

	}

}
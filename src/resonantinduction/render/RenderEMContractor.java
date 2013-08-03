package resonantinduction.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.ResonantInduction;
import resonantinduction.model.ModelEMContractor;

public class RenderEMContractor extends TileEntitySpecialRenderer
{
	public static final ModelEMContractor MODEL = new ModelEMContractor();
	public static final ResourceLocation TEXTURE = new ResourceLocation(ResonantInduction.DOMAIN, ResonantInduction.MODEL_TEXTURE_DIRECTORY + "em_contractor.png");
	
	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
		
		this.func_110628_a(TEXTURE);
		MODEL.render(0.0625f);

		GL11.glPopMatrix();
	}
}

/**
 * 
 */
package resonantinduction.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.ResonantInduction;
import resonantinduction.model.ModelTeslaBottom;
import resonantinduction.model.ModelTeslaMiddle;
import resonantinduction.model.ModelTeslaTop;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderTesla extends TileEntitySpecialRenderer
{
	public static final ResourceLocation TEXTURE_BOTTOM = new ResourceLocation(ResonantInduction.DOMAIN, ResonantInduction.MODEL_TEXTURE_DIRECTORY + "tesla_bottom.png");
	public static final ResourceLocation TEXTURE_MIDDLE = new ResourceLocation(ResonantInduction.DOMAIN, ResonantInduction.MODEL_TEXTURE_DIRECTORY + "tesla_middle.png");
	public static final ResourceLocation TEXTURE_TOP = new ResourceLocation(ResonantInduction.DOMAIN, ResonantInduction.MODEL_TEXTURE_DIRECTORY + "tesla_top.png");
	public static final ModelTeslaBottom MODEL_BOTTOM = new ModelTeslaBottom();
	public static final ModelTeslaMiddle MODEL_MIDDLE = new ModelTeslaMiddle();
	public static final ModelTeslaTop MODEL_TOP = new ModelTeslaTop();

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

		switch (t.getBlockMetadata())
		{
			default:
				this.func_110628_a(TEXTURE_BOTTOM);
				MODEL_BOTTOM.render(0.0625f);
				break;
			case 1:
				this.func_110628_a(TEXTURE_MIDDLE);
				MODEL_MIDDLE.render(0.0625f);
				break;
			case 2:
				this.func_110628_a(TEXTURE_TOP);
				MODEL_TOP.render(0.0625f);
				break;
		}

		GL11.glPopMatrix();
	}

}

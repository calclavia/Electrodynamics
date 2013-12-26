/**
 * 
 */
package resonantinduction.battery;

import java.util.Random;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import resonantinduction.ResonantInduction;
import resonantinduction.model.ModelBattery;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderBattery extends TileEntitySpecialRenderer
{
	public static final ResourceLocation TEXTURE = new ResourceLocation(ResonantInduction.DOMAIN, ResonantInduction.MODEL_TEXTURE_DIRECTORY + "battery.png");
	public static final ResourceLocation TEXTURE_MULTI = new ResourceLocation(ResonantInduction.DOMAIN, ResonantInduction.MODEL_TEXTURE_DIRECTORY + "battery_multi.png");

	public static final ModelBattery MODEL = new ModelBattery();
	private EntityItem fakeBattery;
	private Random random = new Random();
	protected RenderManager renderManager;

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y + 1.5, z + 0.5);
		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

		if (((TileBattery) t).structure.isMultiblock)
		{
			this.bindTexture(TEXTURE_MULTI);
		}
		else
		{
			this.bindTexture(TEXTURE);
		}

		MODEL.render(0.0625f);
		GL11.glPopMatrix();
	}
}

/**
 * 
 */
package resonantinduction.electrical.battery;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.obj.WavefrontObject;

import org.lwjgl.opengl.GL11;

import resonantinduction.core.Reference;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderBattery extends TileEntitySpecialRenderer
{
	public static final ResourceLocation TEXTURE_CAP = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "battery/bat_base_cap_tex.png");
	public static final ResourceLocation TEXTURE_CASE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "battery/bat_case_tex.png");
	public static final ResourceLocation TEXTURE_LEVELS = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "battery/bat_levels.png");
	public static final WavefrontObject MODEL = (WavefrontObject) AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "battery/battery.obj");

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		for (int i = 2; i < 6; i++)
		{
			glPushMatrix();
			glTranslatef((float) x + 0.5F, (float) y, (float) z + 0.5F);
			glScalef(0.46f, 0.46f, 0.46f);
			GL11.glRotatef(90 * i, 0, 1, 0);
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_CASE);
			MODEL.renderOnly("Default");
			glPopMatrix();
		}
	}
}

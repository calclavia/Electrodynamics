package resonantinduction.mechanical.process;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import calclavia.lib.render.RenderUtility;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.obj.WavefrontObject;
import resonantinduction.core.Reference;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class RenderGrinderWheel extends TileEntitySpecialRenderer
{
	public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_PATH + "grinder.png");
	public static final WavefrontObject MODEL = (WavefrontObject) AdvancedModelLoader.loadModel(Reference.MODEL_DIRECTORY + "grinder.obj");

	@Override
	public void renderTileEntityAt(TileEntity t, double x, double y, double z, float f)
	{
		if (t instanceof TileGrinderWheel)
		{
			TileGrinderWheel tile = (TileGrinderWheel) t;
			glPushMatrix();
			glTranslatef((float) x + 0.5F, (float) y + 0.5f, (float) z + 0.5F);
			glScalef(0.51f, 0.51f, 0.51f);
			RenderUtility.rotateBlockBasedOnDirection(tile.getDirection());
			glRotatef((float) Math.toDegrees(tile.getNetwork().getRotation() * (tile.isClockwise() ? 1 : -1)), 0, 0, 1);
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);
			MODEL.renderAll();
			glPopMatrix();
		}
	}
}

package resonantinduction.mechanical.logistic;

import static calclavia.lib.render.ModelUtility.drawCuboid;
import static calclavia.lib.render.ModelUtility.setGlobalTextureResolution;
import static calclavia.lib.render.ModelUtility.setTextureClip;
import static calclavia.lib.render.ModelUtility.setTextureOffset;
import static calclavia.lib.render.ModelUtility.setTextureSubResolution;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslated;
import net.minecraft.tileentity.TileEntity;
import resonantinduction.core.render.RenderImprintable;
import universalelectricity.api.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderDetector extends RenderImprintable
{
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float var8)
	{
		super.renderTileEntityAt(tileEntity, x, y, z, var8);
	}

	public static void render(boolean isInverted, Vector3 position)
	{
		glPushMatrix();
		glTranslated(position.x, position.y, position.z);

		if (isInverted)
		{
			// ForgeHooksClient.bindTexture(AssemblyLine.BLOCK_TEXTURES_PATH + "detector_red.png",
			// 0);
		}
		else
		{
			// ForgeHooksClient.bindTexture(AssemblyLine.BLOCK_TEXTURES_PATH + "detector_green.png",
			// 0);

		}

		setGlobalTextureResolution(128, 128);
		setTextureClip(false);
		setTextureOffset(0, 64);
		setTextureSubResolution(64, 64);
		drawCuboid(0.45f, 12f / 16f, 0.45f, 2f / 16f, 4f / 16f, 2f / 16f); // stand
		setTextureOffset(0, 0);
		setTextureSubResolution(128, 64);
		drawCuboid(0.25f, 0.25f, 0.25f, 8f / 16f, 8f / 16f, 8f / 16f); // block
		setTextureOffset(64, 64);
		setTextureSubResolution(64, 32);
		drawCuboid(0.375f, 0.25f - (1f / 16f), 0.375f, 4f / 16f, 1f / 16f, 4f / 16f); // lens

		glPopMatrix();
	}
}

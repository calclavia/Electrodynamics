package resonantinduction.atomic.machine.thermometer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import resonant.lib.render.RenderUtility;

@SideOnly(Side.CLIENT)
public class RenderThermometer extends TileEntitySpecialRenderer
{
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float var8)
	{
		TileThermometer tile = (TileThermometer) tileEntity;

		GL11.glPushMatrix();
		RenderUtility.enableLightmap();

		for (int side = 2; side < 6; side++)
		{
			RenderUtility.renderText((tile.isOverThreshold() ? "\u00a74" : "") + Math.round(tile.detectedTemperature) + " K", side, 0.8f, x, y + 0.1, z);
			RenderUtility.renderText((tile.isOverThreshold() ? "\u00a74" : "\u00a71") + "Threshold: " + (tile.getThershold()) + " K", side, 1, x, y - 0.1, z);

			if (tile.trackCoordinate != null)
			{
				RenderUtility.renderText(tile.trackCoordinate.xi() + ", " + tile.trackCoordinate.yi() + ", " + tile.trackCoordinate.zi(), side, 0.5f, x, y - 0.3, z);
			}
		}

		GL11.glPopMatrix();

	}
}

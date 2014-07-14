package resonantinduction.archaic.crate;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import resonant.lib.render.RenderItemOverlayUtility;
import resonant.lib.utility.LanguageUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCrate extends TileEntitySpecialRenderer
{
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float var8)
	{
		if (tileEntity instanceof TileCrate)
		{
			GL11.glPushMatrix();
			TileCrate tile = (TileCrate) tileEntity;
			RenderItemOverlayUtility.renderItemOnSides(tileEntity, tile.getSampleStack(), x, y, z, LanguageUtility.getLocal("tooltip.empty"));
			GL11.glPopMatrix();
		}
	}
}

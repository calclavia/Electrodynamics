package resonantinduction.archaic.crate;

import org.lwjgl.opengl.GL11;

import calclavia.lib.utility.LanguageUtility;
import net.minecraft.tileentity.TileEntity;
import resonantinduction.core.render.RenderItemOverlayTile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCrate extends RenderItemOverlayTile
{
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float var8)
	{
		if (tileEntity instanceof TileCrate)
		{
			GL11.glPushMatrix();
			TileCrate tile = (TileCrate) tileEntity;
			renderItemOnSides(tileEntity, tile.getSampleStack(), x, y, z, LanguageUtility.getLocal("tooltip.empty"));
			GL11.glPopMatrix();
		}
	}
}

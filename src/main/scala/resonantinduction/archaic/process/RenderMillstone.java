package resonantinduction.archaic.process;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import resonant.lib.render.RenderItemOverlayUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMillstone extends TileEntitySpecialRenderer
{
	private final RenderBlocks renderBlocks = new RenderBlocks();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float var8)
	{
		if (tileEntity instanceof TileMillstone)
		{
			TileMillstone tile = (TileMillstone) tileEntity;
			RenderItemOverlayUtility.renderItemOnSides(tileEntity, tile.getStackInSlot(0), x, y, z, "");
		}
	}
}

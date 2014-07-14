package resonantinduction.archaic.filter;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonant.lib.render.RenderItemOverlayUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderImprinter extends TileEntitySpecialRenderer
{
	private final RenderBlocks renderBlocks = new RenderBlocks();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float var8)
	{
		if (tileEntity instanceof TileImprinter)
		{
			TileImprinter tile = (TileImprinter) tileEntity;
			RenderItemOverlayUtility.renderTopOverlay(tileEntity, tile.inventory, ForgeDirection.EAST, x, y, z);
			RenderItemOverlayUtility.renderItemOnSides(tileEntity, tile.getStackInSlot(9), x, y, z);
		}
	}
}

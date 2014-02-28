package resonantinduction.archaic.filter;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.render.RenderItemOverlayTile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderImprinter extends RenderItemOverlayTile
{
	private final RenderBlocks renderBlocks = new RenderBlocks();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float var8)
	{
		if (tileEntity instanceof TileImprinter)
		{
			TileImprinter tile = (TileImprinter) tileEntity;
			renderTopOverlay(tileEntity, tile.inventory, ForgeDirection.EAST, x, y, z);
			renderItemOnSides(tileEntity, tile.getStackInSlot(9), x, y, z);
		}
	}
}

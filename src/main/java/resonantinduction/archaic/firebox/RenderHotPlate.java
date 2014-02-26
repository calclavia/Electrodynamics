package resonantinduction.archaic.firebox;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.render.RenderItemOverlayTile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderHotPlate extends RenderItemOverlayTile
{
	private final RenderBlocks renderBlocks = new RenderBlocks();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float var8)
	{
		if (tileEntity instanceof TileHotPlate)
		{
			TileHotPlate tile = (TileHotPlate) tileEntity;
			renderTopOverlay(tileEntity, tile.getInventory().getContainedItems(), ForgeDirection.EAST, 2, 2, x, y - 0.8, z);
		}
	}
}

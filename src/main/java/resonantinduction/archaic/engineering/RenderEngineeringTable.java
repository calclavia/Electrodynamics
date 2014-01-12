package resonantinduction.archaic.engineering;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import resonantinduction.core.render.RenderItemOverlayTile;
import universalelectricity.api.vector.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEngineeringTable extends RenderItemOverlayTile
{
	private final RenderBlocks renderBlocks = new RenderBlocks();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float var8)
	{
		if (tileEntity instanceof TileEngineeringTable)
		{
			TileEngineeringTable tile = (TileEngineeringTable) tileEntity;
			renderTopCrafting(tileEntity, tile.craftingMatrix, x, y, z);
			renderItemOnSides(tileEntity, tile.getStackInSlot(9), x, y, z);
		}
	}
}

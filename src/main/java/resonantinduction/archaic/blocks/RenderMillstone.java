package resonantinduction.archaic.blocks;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.render.RenderItemOverlayTile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderMillstone extends RenderItemOverlayTile
{
	private final RenderBlocks renderBlocks = new RenderBlocks();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float var8)
	{
		if (tileEntity instanceof TileMillstone)
		{
			TileMillstone tile = (TileMillstone) tileEntity;
			renderItemOnSides(tileEntity, tile.getStackInSlot(0), x, y, z, "Empty");
		}
	}
}

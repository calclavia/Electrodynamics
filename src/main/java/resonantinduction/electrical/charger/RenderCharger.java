package resonantinduction.electrical.charger;

import net.minecraft.tileentity.TileEntity;
import resonantinduction.core.render.RenderItemOverlayTile;

public class RenderCharger extends RenderItemOverlayTile
{

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
    {
        if (tile instanceof TileCharger)
        {
            this.renderItemSingleSide(tile, x, y, z, ((TileCharger)tile).getStackInSlot(0), ((TileCharger)tile).getDirection(), "IDLE");
        }
    }

}

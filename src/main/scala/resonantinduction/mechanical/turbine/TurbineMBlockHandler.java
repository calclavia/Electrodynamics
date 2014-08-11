package resonantinduction.mechanical.turbine;

import net.minecraft.tileentity.TileEntity;
import resonant.lib.multiblock.reference.MultiBlockHandler;
import universalelectricity.core.transform.vector.Vector3;

public class TurbineMBlockHandler extends MultiBlockHandler<TileTurbine>
{
    public TurbineMBlockHandler(TileTurbine wrapper)
    {
        super(wrapper);
    }

    public TileTurbine getWrapperAt(Vector3 position)
    {
        TileEntity tile = position.getTileEntity(this.tile.getWorld());

        if (tile != null && wrapperClass.isAssignableFrom(tile.getClass()))
        {
            if (((TileTurbine) tile).getDirection() == this.tile.getDirection() && ((TileTurbine) tile).tier == this.tile.tier)
            {
                return (TileTurbine) tile;
            }
        }

        return null;
    }
}

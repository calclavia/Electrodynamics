package resonantinduction.mechanical.energy.turbine;

import net.minecraft.tileentity.TileEntity;
import resonant.lib.multiblock.MultiBlockHandler;
import universalelectricity.api.vector.Vector3;

public class TurbineMBlockHandler extends MultiBlockHandler<TileTurbine>
{
    public TurbineMBlockHandler(TileTurbine wrapper)
    {
        super(wrapper);
    }

    public TileTurbine getWrapperAt(Vector3 position)
    {
        TileEntity tile = position.getTileEntity(self.getWorld());

        if (tile != null && wrapperClass.isAssignableFrom(tile.getClass()))
        {
            if (((TileTurbine) tile).getDirection() == self.getDirection() && ((TileTurbine) tile).tier == self.tier)
            {
                return (TileTurbine) tile;
            }
        }

        return null;
    }
}

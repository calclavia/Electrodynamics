package resonantinduction.mechanical.energy.turbine;

import net.minecraft.tileentity.TileEntity;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.multiblock.reference.MultiBlockHandler;

public class TurbineMBlockHandler extends MultiBlockHandler<TileTurbineBase>
{
    public TurbineMBlockHandler(TileTurbineBase wrapper)
    {
        super(wrapper);
    }

    public TileTurbineBase getWrapperAt(Vector3 position)
    {
        TileEntity tile = position.getTileEntity(self.getWorld());

        if (tile != null && wrapperClass.isAssignableFrom(tile.getClass()))
        {
            if (((TileTurbineBase) tile).getDirection() == self.getDirection() && ((TileTurbineBase) tile).tier == self.tier)
            {
                return (TileTurbineBase) tile;
            }
        }

        return null;
    }
}

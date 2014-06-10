package resonantinduction.mechanical.process.grinder;

import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.interfaces.IMechanicalNode;
import resonantinduction.mechanical.energy.grid.MechanicalNode;

/** Node just for the grinder
 * 
 * @author Darkguardsman */
public class GrinderNode extends MechanicalNode
{
    public GrinderNode(TileGrindingWheel parent)
    {
        super(parent);
    }

    public TileGrindingWheel grider()
    {
        return (TileGrindingWheel) getParent();
    }

    @Override
    public boolean canConnect(ForgeDirection from, Object source)
    {
        if(grider().getDirection() == ForgeDirection.UP || grider().getDirection() == ForgeDirection.DOWN)
        {
            return grider().getDirection() == from || grider().getDirection().getOpposite() == from;
        }
        return grider().getDirection() != from && grider().getDirection().getOpposite() != from;
    }

    @Override
    public boolean inverseRotation(ForgeDirection dir, IMechanicalNode with)
    {
        return !(dir.offsetX > 0 || dir.offsetZ < 0 || dir.offsetY < 0);
    }
}

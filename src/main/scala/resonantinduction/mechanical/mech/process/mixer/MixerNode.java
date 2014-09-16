package resonantinduction.mechanical.mech.process.mixer;

import net.minecraftforge.common.util.ForgeDirection;
import resonantinduction.mechanical.mech.MechanicalNode;
import universalelectricity.api.core.grid.INodeProvider;

public class MixerNode extends MechanicalNode
{

    public MixerNode(INodeProvider parent)
    {
        super(parent);
        maxDeltaAngle = Math.toRadians(45);
        sharePower = false;
    }

    @Override
    public boolean canConnect(ForgeDirection direction)
    {
        return direction == ForgeDirection.DOWN || direction == ForgeDirection.UP;
    }

    @Override
    public boolean inverseRotation(ForgeDirection dir)
    {
        return dir == ForgeDirection.DOWN;
    }

}

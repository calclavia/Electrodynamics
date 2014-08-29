package resonantinduction.mechanical.process.mixer;

import net.minecraftforge.common.util.ForgeDirection;
import resonantinduction.core.interfaces.IMechanicalNode;
import resonantinduction.mechanical.energy.grid.MechanicalNode;
import universalelectricity.api.core.grid.INode;
import universalelectricity.api.core.grid.INodeProvider;

public class MixerNode extends MechanicalNode
{

    public MixerNode(INodeProvider parent)
    {
        super(parent);
    }

    @Override
    public boolean canConnect(ForgeDirection direction)
    {
        return direction == ForgeDirection.DOWN || direction == ForgeDirection.UP;
    }

    @Override
    public boolean inverseRotation(ForgeDirection dir, IMechanicalNode with)
    {
        return dir == ForgeDirection.DOWN;
    }

}

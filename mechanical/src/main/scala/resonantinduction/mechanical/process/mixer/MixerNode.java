package resonantinduction.mechanical.process.mixer;

import net.minecraftforge.common.ForgeDirection;
import resonant.api.grid.INodeProvider;
import resonantinduction.core.interfaces.IMechanicalNode;
import resonantinduction.mechanical.energy.grid.MechanicalNode;

public class MixerNode extends MechanicalNode
{

    public MixerNode(INodeProvider parent)
    {
        super(parent);
    }
    
    @Override
    public boolean inverseRotation(ForgeDirection dir, IMechanicalNode with)
    {
        return dir == ForgeDirection.DOWN;
    }

}

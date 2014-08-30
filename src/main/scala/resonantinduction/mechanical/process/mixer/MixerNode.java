package resonantinduction.mechanical.process.mixer;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import resonantinduction.core.interfaces.IMechanicalNode;
import resonantinduction.mechanical.energy.grid.MechanicalNode;
import universalelectricity.api.core.grid.INode;
import universalelectricity.api.core.grid.INodeProvider;

import java.util.WeakHashMap;

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

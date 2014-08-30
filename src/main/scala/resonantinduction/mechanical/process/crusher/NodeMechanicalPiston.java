package resonantinduction.mechanical.process.crusher;

import net.minecraftforge.common.util.ForgeDirection;
import resonantinduction.mechanical.energy.grid.MechanicalNode;
import resonantinduction.mechanical.energy.grid.TileMechanical;
import universalelectricity.api.core.grid.INodeProvider;

/**
 * Created by robert on 8/28/2014.
 */
public class NodeMechanicalPiston extends MechanicalNode
{
    public NodeMechanicalPiston(TileMechanicalPiston parent)
    {
        super(parent);
        maxDeltaAngle = Math.toRadians(45);
        sharePower = false;
    }

    @Override
    public boolean canConnect(ForgeDirection dir)
    {
        return dir != ((TileMechanicalPiston)getParent()).getDirection();
    }

    @Override
    protected void revolve()
    {
        ((TileMechanicalPiston)getParent()).markRevolve = true;
    }
}

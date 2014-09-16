package resonantinduction.mechanical.mech.process.crusher;

import net.minecraftforge.common.util.ForgeDirection;
import resonantinduction.mechanical.mech.MechanicalNode;

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

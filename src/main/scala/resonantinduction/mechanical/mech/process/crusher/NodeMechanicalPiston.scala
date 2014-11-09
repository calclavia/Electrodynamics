package resonantinduction.mechanical.mech.process.crusher

import net.minecraftforge.common.util.ForgeDirection
import resonantinduction.mechanical.mech.grid.MechanicalNode

/**
 * Created by robert on 8/28/2014.
 */
class NodeMechanicalPiston(parent: TileMechanicalPiston) extends MechanicalNode(parent)
{
    override def canConnect(dir: ForgeDirection): Boolean =
    {
        return dir ne (getParent.asInstanceOf[TileMechanicalPiston]).getDirection
    }

    protected override def revolve
    {
        getParent.asInstanceOf[TileMechanicalPiston].markRevolve = true
    }
}
package resonantinduction.mechanical.mech.process.crusher

import net.minecraftforge.common.util.ForgeDirection
import resonantinduction.mechanical.mech.grid.NodeMechanical

/**
 * Created by robert on 8/28/2014.
 */
class NodeMechanicalPiston(parent: TileMechanicalPiston) extends NodeMechanical(parent)
{
    override def canConnect(dir: ForgeDirection): Boolean =
    {
        return dir ne (getParent.asInstanceOf[TileMechanicalPiston]).getDirection
    }

    protected def revolve
    {
        getParent.asInstanceOf[TileMechanicalPiston].markRevolve = true
    }
}
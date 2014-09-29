package resonantinduction.mechanical.mech.turbine

import resonantinduction.mechanical.mech.MechanicalNode
import net.minecraftforge.common.util.ForgeDirection

/**
 * Turbine's Mechanical node
 * Turbines always face forward and connect from behind.
 *
 * @author Calclavia, Darkguardsman
 */
class TurbineNode(tileTurbineBase: TileTurbine) extends MechanicalNode(tileTurbineBase)
{

    def turbine: TileTurbine =
    {
        return getParent.asInstanceOf[TileTurbine]
    }

    override def canConnect(from: ForgeDirection, source: AnyRef): Boolean =
    {
        return turbine.getMultiBlock.isPrimary && source.isInstanceOf[MechanicalNode] && !(source.isInstanceOf[TurbineNode]) && from == turbine.getDirection
    }

    override def inverseRotation(dir: ForgeDirection): Boolean =
    {
        return dir == turbine.getDirection.getOpposite
    }
}
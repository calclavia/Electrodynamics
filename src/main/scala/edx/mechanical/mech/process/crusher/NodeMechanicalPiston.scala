package edx.mechanical.mech.process.crusher

import edx.mechanical.mech.grid.NodeMechanical
import net.minecraftforge.common.util.ForgeDirection

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
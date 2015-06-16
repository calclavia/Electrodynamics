package com.calclavia.edx.mechanical.mech.process.crusher

import com.calclavia.edx.mechanical.mech.grid.MechanicalComponent
import net.minecraftforge.common.util.ForgeDirection

/**
 * Created by robert on 8/28/2014.
 */
class NodeMechanicalPiston(parent: TileMechanicalPiston) extends MechanicalComponent(parent)
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
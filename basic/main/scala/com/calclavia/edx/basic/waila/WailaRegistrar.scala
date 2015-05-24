package com.calclavia.edx.basic.waila

import com.calclavia.edx.basic.fluid.tank.TileTank
import mcp.mobius.waila.api.IWailaRegistrar

object WailaRegistrar
{
  def wailaCallBack(registrar: IWailaRegistrar)
  {
    registrar.registerBodyProvider(new WailaFluidTank, classOf[TileTank])
  }
}
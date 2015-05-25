package com.calclavia.edx.basic.waila

import cpw.mods.fml.common.event.FMLInterModComms
import resonantengine.lib.mod.compat.Mods
import resonantengine.lib.mod.loadable.ICompatProxy

/**
 * @author tgame14
 * @since 21/03/14
 */
class Waila extends ICompatProxy
{
  def preInit
  {
  }

  def init
  {
    FMLInterModComms.sendMessage(Mods.WAILA, "register", "resonantinduction.archaic.waila.WailaRegistrar.wailaCallBack")
  }

  def postInit
  {
  }

  def modId: String =
  {
    return Mods.WAILA
  }
}
package com.calclavia.edx.mechanical

import com.calclavia.edx.core.Reference
import nova.core.loader.{Loadable, NovaMod}

@NovaMod(
  id = Reference.mechanicID,
  name = Reference.name + ": Mechanic",
  version = Reference.version,
  novaVersion = Reference.novaVersion,
  dependencies = Array("microblock"),
  domains = Array("edx")
)
object Mechanic extends Loadable {

	override def preInit(): Unit = MechanicContent.preInit()

	override def init(): Unit = MechanicContent.init()

	override def postInit(): Unit = MechanicContent.postInit()

}

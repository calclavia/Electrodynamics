package com.calclavia.edx.core.component

import nova.core.component.Component
import nova.core.network.{Packet, Sync, PacketHandler}
import nova.core.retention.{Stored, Storable}

/**
 * An object that contains a certain material componentType.
 * @author Calclavia
 */
abstract class Material[TYPE] extends Component with Storable with PacketHandler{
	@Stored
	@Sync
	var material: TYPE = _
}

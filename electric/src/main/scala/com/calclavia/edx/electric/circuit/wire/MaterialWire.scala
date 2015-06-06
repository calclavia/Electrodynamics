package com.calclavia.edx.electric.circuit.wire

import nova.core.component.Component
import nova.core.network.{Packet, Syncable}
import nova.core.retention.{Storable, Stored}

/**
 * @author Calclavia
 */
class MaterialWire extends Component with Storable with Syncable {
	@Stored
	var material = WireMaterial.UNKNOWN

	//TODO: Packet enum reading doesn't work
	override def read(packet: Packet) {
		material = WireMaterial.values()(packet.readInt())
	}

	override def write(packet: Packet) {
		packet.writeInt(material.ordinal)
	}
}

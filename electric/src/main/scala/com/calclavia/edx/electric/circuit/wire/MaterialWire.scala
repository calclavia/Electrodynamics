package com.calclavia.edx.electric.circuit.wire

import com.calclavia.edx.core.component.Material
import nova.core.network.Packet

/**
 * @author Calclavia
 */
class MaterialWire extends Material[WireMaterial] {
	material = WireMaterial.UNKNOWN

	//TODO: Packet enum reading doesn't work
	override def read(packet: Packet) {
		material = WireMaterial.values()(packet.readInt())
	}

	override def write(packet: Packet) {
		packet.writeInt(material.ordinal)
	}
}

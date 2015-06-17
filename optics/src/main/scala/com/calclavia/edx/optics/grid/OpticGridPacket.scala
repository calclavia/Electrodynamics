package com.calclavia.edx.optics.grid

import com.calclavia.edx.core.EDX
import nova.core.network.Packet
import nova.core.network.handler.PacketHandler

/**
 * @author Calclavia
 */
/**
 * Handles the packets for waves.
 */
class OpticGridPacket extends PacketHandler[OpticGrid] {
	override def read(packet: Packet) {
		val worldID = packet.readString()
		val opWorld = EDX.worlds.findWorld(worldID)
		if (opWorld.isPresent) {
			val world = opWorld.get
			val grid = OpticGrid(world)

			grid.sources.synchronized {
				grid.sources = Set.empty

				//Read graph
				(0 until packet.readInt())
					.foreach(i => {
					val newBeam = packet.readStorable().asInstanceOf[Beam]
					newBeam.world = world
					grid.sources += newBeam
				})
			}
		}
		else {
			throw new RuntimeException("Failed to read wave graph for invalid world: " + opWorld)
		}
	}

	override def write(handler: OpticGrid, packet: Packet) {
		handler.sources.synchronized {
			packet.writeString(handler.world.getID)
			//Write sources
			val sources = handler.sources
			packet.writeInt(sources.size)
			sources.foreach(packet.writeStorable)
		}
	}

	override def isHandlerFor(handler: AnyRef): Boolean = handler.isInstanceOf[OpticGrid]
}
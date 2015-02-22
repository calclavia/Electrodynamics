package mffs.base

import com.resonant.core.prefab.block.InventorySimpleProvider
import nova.core.network.Packet

/**
 * All TileEntities that have an inventory should extend this.
 *
 * @author Calclavia
 */
abstract class TileMFFSInventory extends BlockMFFS with InventorySimpleProvider {
	override def read(id: Int, packet: Packet) {
		super.read(id, packet)

		if (id == TilePacketType.description.id || id == TilePacketType.inventory.id) {
			inventory.read(id, packet)
		}
	}

	override def write(id: Int, packet: Packet) {
		super.write(id, packet)
		if (id == TilePacketType.description.id) {
			inventory.write(id, packet)
		}
	}
}
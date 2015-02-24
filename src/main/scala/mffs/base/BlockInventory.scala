package mffs.base

import com.resonant.core.prefab.block.InventorySimpleProvider
import nova.core.inventory.InventorySimple
import nova.core.network.Packet
import nova.core.retention.Stored

/**
 * All blocks that have an inventory should extend this.
 *
 * @author Calclavia
 */
abstract class BlockInventory extends BlockMFFS with InventorySimpleProvider {
	@Stored
	protected val inventory: InventorySimple

	override def read(id: Int, packet: Packet) {
		super.read(id, packet)

		if (id == PacketBlock.description.ordinal() || id == PacketBlock.inventory.ordinal()) {
			inventory.read(id, packet)
		}
	}

	override def write(id: Int, packet: Packet) {
		super.write(id, packet)
		if (id == PacketBlock.description.ordinal()) {
			inventory.write(id, packet)
		}
	}
}
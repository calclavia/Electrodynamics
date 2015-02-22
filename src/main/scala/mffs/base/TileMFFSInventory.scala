package mffs.base

import com.resonant.core.prefab.block.InventorySimpleProvider
import nova.core.network.Packet

/**
 * All TileEntities that have an inventory should extend this.
 *
 * @author Calclavia
 */
abstract class TileMFFSInventory extends BlockMFFS with InventorySimpleProvider {
	override def write(buf: Packet, id: Int) {
		super.write(buf, id)

		if (id == TilePacketType.description.id) {
			val nbt = new NBTTagCompound
			getInventory.save(nbt)
			buf <<< nbt
		}
	}

	override def read(buf: Packet, id: Int, packetType: PacketType) {
		super.read(buf, id, packetType)

		if (id == TilePacketType.description.id || id == TilePacketType.inventory.id) {
			getInventory.load(buf.readTag())
		}
	}
}
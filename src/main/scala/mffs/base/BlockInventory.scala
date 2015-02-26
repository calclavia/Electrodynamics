package mffs.base

import com.resonant.core.prefab.block.InventorySimpleProvider
import nova.core.inventory.InventorySimple
import nova.core.network.Sync
import nova.core.retention.Stored

/**
 * All blocks that have an inventory should extend this.
 *
 * @author Calclavia
 */
abstract class BlockInventory extends BlockMFFS with InventorySimpleProvider {

	@Stored
	@Sync(ids = Array(PacketBlock.description.ordinal(), PacketBlock.inventory.ordinal()))
	override protected val inventory: InventorySimple

}
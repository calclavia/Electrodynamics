package resonantinduction.electrical.encoder.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import resonantinduction.electrical.encoder.ItemDisk;
import resonantinduction.electrical.encoder.TileEncoder;
import calclavia.lib.gui.ContainerBase;
import calclavia.lib.prefab.slot.SlotSpecific;

public class ContainerEncoder extends ContainerBase
{
	private ItemStack[] containingItems = new ItemStack[1];
	private TileEncoder tileEntity;

	public ContainerEncoder(InventoryPlayer inventoryPlayer, TileEncoder encoder)
	{
		super(encoder);
		this.tileEntity = encoder;
		// Disk
		addSlotToContainer(new SlotSpecific(encoder, 0, 80, 24, ItemDisk.class));
		addPlayerInventory(inventoryPlayer.player);
	}
}

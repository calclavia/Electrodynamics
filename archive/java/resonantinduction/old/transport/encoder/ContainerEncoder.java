package resonantinduction.old.transport.encoder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import dark.lib.prefab.invgui.SlotSpecific;

public class ContainerEncoder extends Container
{
	public static final int Y_OFFSET = 0;

	private ItemStack[] containingItems = new ItemStack[1];
	private TileEntityEncoder tileEntity;

	public ContainerEncoder(InventoryPlayer inventoryPlayer, TileEntityEncoder encoder)
	{
		this.tileEntity = encoder;

		// Disk
		this.addSlotToContainer(new SlotSpecific(encoder, 0, 80, 24 + Y_OFFSET, ItemDisk.class));

		int row;

		// Player Inventory
		for (row = 0; row < 3; ++row)
		{
			for (int slot = 0; slot < 9; ++slot)
			{
				this.addSlotToContainer(new Slot(inventoryPlayer, slot + row * 9 + 9, 8 + slot * 18, 97 + row * 8));
			}
		}
		for (row = 0; row < 9; ++row)
		{
			this.addSlotToContainer(new Slot(inventoryPlayer, row, 8 + row * 18, 145));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return this.tileEntity.isUseableByPlayer(player);
	}

	/** Called to transfer a stack from one inventory to the other eg. when shift clicking. */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		ItemStack copyStack = null;
		Slot slotObj = (Slot) this.inventorySlots.get(slot);

		if (slotObj != null && slotObj.getHasStack())
		{
			ItemStack slotStack = slotObj.getStack();
			copyStack = slotStack.copy();

			if (slot >= 1)
			{
				if (this.getSlot(0).isItemValid(slotStack))
				{
					if (!this.mergeItemStack(slotStack, 0, 1, false))
					{
						return null;
					}
				}
			}
			else if (!this.mergeItemStack(slotStack, this.containingItems.length, 37, false))
			{
				return null;
			}

			if (slotStack.stackSize == 0)
			{
				slotObj.putStack((ItemStack) null);
			}
			else
			{
				slotObj.onSlotChanged();
			}

			if (slotStack.stackSize == copyStack.stackSize)
			{
				return null;
			}

			slotObj.onPickupFromSlot(player, slotStack);
		}

		return copyStack;
	}
}

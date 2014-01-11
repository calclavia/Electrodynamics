package resonantinduction.archaic.engineering;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import calclavia.lib.prefab.slot.ISlotWatcher;
import calclavia.lib.prefab.slot.SlotCraftingResult;
import calclavia.lib.prefab.slot.SlotSpecific;
import calclavia.lib.prefab.slot.SlotWatched;

public class ContainerEngineering extends Container implements ISlotWatcher
{
	public InventoryPlayer inventoryPlayer;
	public TileImprinter tileEntity;

	public ContainerEngineering(InventoryPlayer inventoryPlayer, TileImprinter tileEntity)
	{
		this.tileEntity = tileEntity;
		this.tileEntity.container = this;
		this.inventoryPlayer = inventoryPlayer;

		/** Crafting Matrix */
		for (int x = 0; x < 3; x++)
		{
			for (int y = 0; y < 3; y++)
			{
				this.addSlotToContainer(new SlotWatched(this.tileEntity, y + x * 3, 9 + y * 18, 16 + x * 18, this));
			}
		}

		// Imprint Input for Imprinting
		this.addSlotToContainer(new SlotSpecific(this.tileEntity, TileImprinter.IMPRINTER_MATRIX_START, 68, 34, ItemBlockFilter.class));
		// Item to be imprinted
		this.addSlotToContainer(new SlotWatched(this.tileEntity, TileImprinter.IMPRINTER_MATRIX_START + 1, 92, 34, this));
		// Result of Crafting/Imprinting
		this.addSlotToContainer(new SlotCraftingResult(this.tileEntity, this, this.tileEntity, TileImprinter.IMPRINTER_MATRIX_START + 2, 148, 34));

		// Imprinter Inventory
		for (int ii = 0; ii < 2; ii++)
		{
			for (int i = 0; i < 9; i++)
			{
				this.addSlotToContainer(new SlotWatched(this.tileEntity, (i + ii * 9) + TileImprinter.INVENTORY_START, 8 + i * 18, 80 + ii * 18, this));
			}
		}

		// Player Inventory
		int var3;

		for (var3 = 0; var3 < 3; ++var3)
		{
			for (int var4 = 0; var4 < 9; ++var4)
			{
				this.addSlotToContainer(new SlotWatched(inventoryPlayer, var4 + var3 * 9 + 9, 8 + var4 * 18, 120 + var3 * 18, this));
			}
		}

		for (var3 = 0; var3 < 9; ++var3)
		{
			this.addSlotToContainer(new SlotWatched(inventoryPlayer, var3, 8 + var3 * 18, 178, this));
		}

		this.tileEntity.openChest();
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer)
	{
		super.onContainerClosed(par1EntityPlayer);
		this.tileEntity.closeChest();
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

			if (slot == TileImprinter.INVENTORY_START - 1)
			{
				// Prevents filter from being duplicated
				this.tileEntity.setInventorySlotContents(TileImprinter.INVENTORY_START - 1, null);
			}

			if (slot > this.tileEntity.getSizeInventory() - 1)
			{
				if (this.getSlot(TileImprinter.IMPRINTER_MATRIX_START).isItemValid(slotStack))
				{
					if (!this.mergeItemStack(slotStack, TileImprinter.IMPRINTER_MATRIX_START, TileImprinter.IMPRINTER_MATRIX_START + 1, true))
					{
						return null;
					}
				}
				else if (!this.mergeItemStack(slotStack, TileImprinter.INVENTORY_START, this.tileEntity.getSizeInventory(), false))
				{
					return null;
				}
			}
			else if (!this.mergeItemStack(slotStack, this.tileEntity.getSizeInventory(), this.tileEntity.getSizeInventory() + 36, false))
			{
				return null;
			}

			if (slotStack.stackSize == 0)
			{
				slotObj.putStack(null);
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

		this.slotContentsChanged(slot);
		return copyStack;
	}

	@Override
	public void slotContentsChanged(int slot)
	{
		this.tileEntity.onInventoryChanged();
		this.detectAndSendChanges();
	}
}

package mffs.base;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBase extends Container
{
	protected int slotCount = 0;
	private IInventory inventory;

	public ContainerBase(IInventory inventory)
	{
		this.inventory = inventory;
		this.slotCount = inventory.getSizeInventory();
	}

	@Override
	public void onCraftGuiClosed(EntityPlayer player)
	{
		if (this.inventory instanceof TileEntityBase)
		{
			((TileEntityBase) this.inventory).playersUsing.remove(player);
		}
	}

	public void addPlayerInventory(EntityPlayer player)
	{
		if (this.inventory instanceof TileEntityBase)
		{
			((TileEntityBase) this.inventory).playersUsing.add(player);
		}

		for (int var3 = 0; var3 < 3; var3++)
		{
			for (int var4 = 0; var4 < 9; var4++)
			{
				this.addSlotToContainer(new Slot(player.inventory, var4 + var3 * 9 + 9, 8 + var4 * 18, 135 + var3 * 18));
			}

		}

		for (int var3 = 0; var3 < 9; var3++)
		{
			this.addSlotToContainer(new Slot(player.inventory, var3, 8 + var3 * 18, 193));
		}
	}

	/**
	 * Called to transfer a stack from one inventory to the other eg. when shift clicking.
	 */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slotID)
	{
		ItemStack var2 = null;
		// try
		{
			Slot var3 = (Slot) this.inventorySlots.get(slotID);

			if (var3 != null && var3.getHasStack())
			{
				ItemStack itemStack = var3.getStack();
				var2 = itemStack.copy();

				// A slot ID greater than the slot count means it is inside the TileEntity GUI.
				if (slotID >= this.slotCount)
				{
					// Player Inventory, Try to place into slot.
					boolean didTry = false;

					for (int i = 0; i < this.slotCount; i++)
					{
						if (this.getSlot(i).isItemValid(itemStack))
						{
							didTry = true;

							if (this.mergeItemStack(itemStack, i, i + 1, false))
							{
								break;
							}
						}
					}

					if (!didTry)
					{
						if (slotID < 27 + this.slotCount)
						{
							if (!this.mergeItemStack(itemStack, 27 + this.slotCount, 36 + this.slotCount, false))
							{
								return null;
							}
						}
						else if (slotID >= 27 + this.slotCount && slotID < 36 + this.slotCount && !this.mergeItemStack(itemStack, slotCount, 27 + slotCount, false))
						{
							return null;
						}
					}
				}
				else if (!this.mergeItemStack(itemStack, this.slotCount, 36 + this.slotCount, false))
				{
					return null;
				}

				if (itemStack.stackSize == 0)
				{
					var3.putStack((ItemStack) null);
				}
				else
				{
					var3.onSlotChanged();
				}

				if (itemStack.stackSize == var2.stackSize)
				{
					return null;
				}

				var3.onPickupFromSlot(par1EntityPlayer, itemStack);
			}
		}
		/*
		 * catch (Exception e) { e.printStackTrace(); }
		 */

		return var2;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return this.inventory.isUseableByPlayer(entityplayer);
	}
}

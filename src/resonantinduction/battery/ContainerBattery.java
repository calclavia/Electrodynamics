package resonantinduction.battery;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import resonantinduction.battery.BatteryManager.SlotBattery;
import resonantinduction.battery.BatteryManager.SlotOut;
import universalelectricity.core.item.IItemElectric;

public class ContainerBattery extends Container
{
	private TileEntityBattery tileEntity;

	public ContainerBattery(InventoryPlayer inventory, TileEntityBattery unit)
	{
		tileEntity = unit;
		addSlotToContainer(new SlotBattery(unit, 0, 8, 22));
		addSlotToContainer(new SlotOut(unit, 1, 8, 58));
		addSlotToContainer(new SlotBattery(unit, 2, 31, 22));
		addSlotToContainer(new SlotBattery(unit, 3, 31, 58));

		int slotX;

		for (slotX = 0; slotX < 3; ++slotX)
		{
			for (int slotY = 0; slotY < 9; ++slotY)
			{
				addSlotToContainer(new Slot(inventory, slotY + slotX * 9 + 9, 8 + slotY * 18, 125 + slotX * 18));
			}
		}

		for (slotX = 0; slotX < 9; ++slotX)
		{
			addSlotToContainer(new Slot(inventory, slotX, 8 + slotX * 18, 183));
		}

		tileEntity.openChest();
		tileEntity.playersUsing.add(inventory.player);
	}

	@Override
	public ItemStack slotClick(int slotID, int par2, int par3, EntityPlayer par4EntityPlayer)
	{
		ItemStack stack = super.slotClick(slotID, par2, par3, par4EntityPlayer);

		if (slotID == 1)
		{
			ItemStack itemstack = ((Slot) inventorySlots.get(slotID)).getStack();
			ItemStack itemstack1 = itemstack == null ? null : itemstack.copy();
			inventoryItemStacks.set(slotID, itemstack1);

			for (int j = 0; j < crafters.size(); ++j)
			{
				((ICrafting) crafters.get(j)).sendSlotContents(this, slotID, itemstack1);
			}
		}

		return stack;
	}

	@Override
	public void onContainerClosed(EntityPlayer entityplayer)
	{
		super.onContainerClosed(entityplayer);
		tileEntity.closeChest();
		tileEntity.playersUsing.remove(entityplayer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return tileEntity.isUseableByPlayer(entityplayer);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID)
	{
		ItemStack stack = null;
		Slot currentSlot = (Slot) inventorySlots.get(slotID);

		if (currentSlot != null && currentSlot.getHasStack())
		{
			ItemStack slotStack = currentSlot.getStack();
			stack = slotStack.copy();

			if (slotID == 0 || slotID == 1)
			{
				if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true))
				{
					return null;
				}
			}
			else if (slotID == 2 || slotID == 3)
			{
				if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true))
				{
					return null;
				}
			}
			else if (slotStack.getItem() instanceof IItemElectric)
			{
				if (!mergeItemStack(slotStack, 0, 1, false))
				{
					return null;
				}
			}
			else
			{
				if (slotID >= 4 && slotID <= 30)
				{
					if (!mergeItemStack(slotStack, 31, inventorySlots.size(), false))
					{
						return null;
					}
				}
				else if (slotID > 30)
				{
					if (!mergeItemStack(slotStack, 4, 30, false))
					{
						return null;
					}
				}
				else
				{
					if (!mergeItemStack(slotStack, 4, inventorySlots.size(), true))
					{
						return null;
					}
				}
			}

			if (slotStack.stackSize == 0)
			{
				currentSlot.putStack((ItemStack) null);
			}
			else
			{
				currentSlot.onSlotChanged();
			}

			if (slotStack.stackSize == stack.stackSize)
			{
				return null;
			}

			currentSlot.onPickupFromSlot(player, slotStack);
		}

		return stack;
	}
}

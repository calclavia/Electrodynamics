package resonantinduction.old.mechanics.processor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import resonantinduction.old.lib.recipes.MachineRecipeHandler;
import universalelectricity.api.item.IEnergyItem;

public class ContainerProcessor extends Container
{
	private TileEntityProcessor tileEntity;
	private int lastCookTime;
	private int lastEnergyLevel;
	private int lastItemBurnTime;

	public ContainerProcessor(InventoryPlayer par1InventoryPlayer, TileEntityProcessor tile)
	{
		this.tileEntity = tile;
		this.addSlotToContainer(new Slot(tile, tile.slotInput, 37, 25));
		this.addSlotToContainer(new Slot(tile, tile.slotBatteryCharge, 144, 19));
		this.addSlotToContainer(new Slot(tile, tile.slotBatteryDrain, 144, 43));
		this.addSlotToContainer(new SlotFurnace(par1InventoryPlayer.player, tile, tile.slotOutput, 95, 27));

		int i;
		for (i = 0; i < 3; ++i)
		{
			for (int j = 0; j < 9; ++j)
			{
				this.addSlotToContainer(new Slot(par1InventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (i = 0; i < 9; ++i)
		{
			this.addSlotToContainer(new Slot(par1InventoryPlayer, i, 8 + i * 18, 142));
		}
	}

	@Override
	public void addCraftingToCrafters(ICrafting par1ICrafting)
	{
		super.addCraftingToCrafters(par1ICrafting);
		par1ICrafting.sendProgressBarUpdate(this, 0, this.tileEntity.processingTime);
		par1ICrafting.sendProgressBarUpdate(this, 1, (int) this.tileEntity.getEnergyStored());
		par1ICrafting.sendProgressBarUpdate(this, 2, this.tileEntity.processingTicks);
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		for (int i = 0; i < this.crafters.size(); ++i)
		{
			ICrafting icrafting = (ICrafting) this.crafters.get(i);

			if (this.lastCookTime != this.tileEntity.processingTime)
			{
				icrafting.sendProgressBarUpdate(this, 0, this.tileEntity.processingTime);
			}

			if (this.lastEnergyLevel != this.tileEntity.getEnergyStored())
			{
				icrafting.sendProgressBarUpdate(this, 1, (int) this.tileEntity.getEnergyStored());
			}

			if (this.lastItemBurnTime != this.tileEntity.processingTicks)
			{
				icrafting.sendProgressBarUpdate(this, 2, this.tileEntity.processingTicks);
			}
		}

		this.lastCookTime = this.tileEntity.processingTime;
		this.lastEnergyLevel = (int) this.tileEntity.getEnergyStored();
		this.lastItemBurnTime = this.tileEntity.processingTicks;
	}

	@Override
	public boolean canInteractWith(EntityPlayer par1EntityPlayer)
	{
		return this.tileEntity.isUseableByPlayer(par1EntityPlayer);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID)
	{
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(slotID);

		if (slot != null && slot.getHasStack())
		{
			ItemStack slotStack = slot.getStack();
			itemstack = slotStack.copy();

			if (slotID == 2)
			{
				if (!this.mergeItemStack(slotStack, 3, 39, true))
				{
					return null;
				}

				slot.onSlotChange(slotStack, itemstack);
			}
			else if (slotID != 1 && slotID != 0)
			{
				if (MachineRecipeHandler.getProcessorOutput(tileEntity.getProcessorData().type, slotStack) != null)
				{
					if (!this.mergeItemStack(slotStack, tileEntity.slotInput, 1, false))
					{
						return null;
					}
				}
				else if (slotStack.getItem() instanceof IEnergyItem)
				{
					if (!this.mergeItemStack(slotStack, tileEntity.slotBatteryCharge, 2, false))
					{
						return null;
					}
				}
				else if (slotID >= 3 && slotID < 30)
				{
					if (!this.mergeItemStack(slotStack, 30, 39, false))
					{
						return null;
					}
				}
				else if (slotID >= 30 && slotID < 39 && !this.mergeItemStack(slotStack, 3, 30, false))
				{
					return null;
				}
			}
			else if (!this.mergeItemStack(slotStack, 3, 39, false))
			{
				return null;
			}

			if (slotStack.stackSize == 0)
			{
				slot.putStack((ItemStack) null);
			}
			else
			{
				slot.onSlotChanged();
			}

			if (slotStack.stackSize == itemstack.stackSize)
			{
				return null;
			}

			slot.onPickupFromSlot(player, slotStack);
		}

		return itemstack;
	}
}

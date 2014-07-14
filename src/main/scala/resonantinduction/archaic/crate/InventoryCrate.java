package resonantinduction.archaic.crate;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import resonant.lib.utility.inventory.ExternalInventory;

public class InventoryCrate extends ExternalInventory
{
	public InventoryCrate(TileEntity crate)
	{
		super(crate, 512);
	}

	/** Clones the single stack into an inventory format for automation interaction */
	public void buildInventory(ItemStack sampleStack)
	{
		this.containedItems = new ItemStack[this.getSizeInventory()];
		if (sampleStack != null && sampleStack.getItem() != null)
		{
			ItemStack baseStack = sampleStack.copy();
			int itemsLeft = baseStack.stackSize;

			for (int slot = 0; slot < this.getContainedItems().length; slot++)
			{
				int stackL = Math.min(Math.min(itemsLeft, baseStack.getMaxStackSize()), this.getInventoryStackLimit());
				this.getContainedItems()[slot] = baseStack.copy();
				this.getContainedItems()[slot].stackSize = stackL;
				itemsLeft -= stackL;
				if (baseStack.stackSize <= 0)
				{
					baseStack = null;
					break;
				}
			}
		}
	}

	@Override
	public int getSizeInventory()
	{
		if (this.hostTile instanceof TileCrate)
		{
			return ((TileCrate) this.hostTile).getSlotCount();
		}
		return 512;
	}

	@Override
	public String getInvName()
	{
		return "inv.Crate";
	}

	@Override
	public void save(NBTTagCompound nbt)
	{

	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		if (nbt.hasKey("Items"))
		{
			super.load(nbt);
		}

	}
}
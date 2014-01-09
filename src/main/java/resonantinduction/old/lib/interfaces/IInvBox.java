package resonantinduction.old.lib.interfaces;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** @author DarkGuardsman */
public interface IInvBox extends ISidedInventory
{
	/** Gets the inventory array. ForgeDirection.UNKOWN must return all sides */
	public ItemStack[] getContainedItems();

	/** Called to save the inventory array */
	public NBTTagCompound saveInv(NBTTagCompound tag);

	/** Called to load the inventory array */
	public void loadInv(NBTTagCompound tag);

	/** Dels all the items in the inventory */
	public void clear();
}

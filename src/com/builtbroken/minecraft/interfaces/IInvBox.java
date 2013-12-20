package com.builtbroken.minecraft.interfaces;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** External inventory management container for an object. Allows for most if not all inventory code
 * to be removed from the tile. That is some methods will still need to remain in order to work with
 * automation. As well this is not designed to replace the need for IInventory support of a tile but
 * to make it easier to manage. Suggested use it to create a prefab manager for several tiles. Then
 * have those tiles use the prefab as an extermal inventory manager to reduce code size per class.
 * 
 * @author DarkGuardsman */
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

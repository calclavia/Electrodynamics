package com.dark.helpers;

import java.util.ArrayList;
import java.util.List;

import com.dark.interfaces.IExtendedStorage;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;

/** Helper that handles most of the interaction of the tile with the inventories around it
 *
 * @author Rseifert */
public class InvInteractionHelper
{
    public World world;
    Vector3 location;
    List<ItemStack> filteredItems;
    boolean inverted;

    public InvInteractionHelper(World world, Vector3 location, List<ItemStack> filters, boolean inverted)
    {
        this.world = world;
        this.location = location;
        this.filteredItems = filters;
        if (filteredItems == null)
        {
            filteredItems = new ArrayList<ItemStack>();
        }
        this.inverted = inverted;
    }

    public void setFilter(List<ItemStack> filters, boolean inverted)
    {
        this.filteredItems = filters;
        this.inverted = inverted;
    }

    /** Throws the items from the manipulator into the world.
     *
     * @param outputPosition
     * @param items */
    public void throwItem(Vector3 outputPosition, ItemStack items)
    {
        if (!world.isRemote)
        {
            EntityItem entityItem = new EntityItem(world, outputPosition.x + 0.5, outputPosition.y + 0.8, outputPosition.z + 0.5, items);
            entityItem.motionX = 0;
            entityItem.motionZ = 0;
            entityItem.motionY /= 5;
            entityItem.delayBeforeCanPickup = 20;
            world.spawnEntityInWorld(entityItem);
        }
    }

    public ItemStack storeItem(ItemStack item, ForgeDirection... directions)
    {
        if (item != null)
        {
            ItemStack remainingStack = item.copy();
            for (ForgeDirection direction : directions)
            {
                remainingStack = tryPlaceInPosition(remainingStack, this.location.clone().modifyPositionFromSide(direction), direction.getOpposite());
            }
            return remainingStack;
        }
        return item;
    }

    /** Tries to place an itemStack in a specific position if it is an inventory.
     *
     * @return The ItemStack remained after place attempt */
    public ItemStack tryPlaceInPosition(ItemStack itemStack, Vector3 position, ForgeDirection dir)
    {
        TileEntity tileEntity = position.getTileEntity(world);
        ForgeDirection direction = dir.getOpposite();

        if (tileEntity != null && itemStack != null)
        {
            if (tileEntity instanceof TileEntityChest)
            {
                TileEntityChest[] chests = { (TileEntityChest) tileEntity, null };

                /** Try to find a double chest. */
                for (int i = 2; i < 6; i++)
                {
                    ForgeDirection searchDirection = ForgeDirection.getOrientation(i);
                    Vector3 searchPosition = position.clone();
                    searchPosition.modifyPositionFromSide(searchDirection);

                    if (searchPosition.getTileEntity(world) != null)
                    {
                        if (searchPosition.getTileEntity(world).getClass() == chests[0].getClass())
                        {
                            chests[1] = (TileEntityChest) searchPosition.getTileEntity(world);
                            break;
                        }
                    }
                }

                for (TileEntityChest chest : chests)
                {
                    if (chest != null)
                    {
                        for (int i = 0; i < chest.getSizeInventory(); i++)
                        {
                            itemStack = this.addStackToInventory(i, chest, itemStack);
                            if (itemStack == null)
                            {
                                return null;
                            }
                        }
                    }
                }
            }
            else if (tileEntity instanceof IExtendedStorage)
            {
                return ((IExtendedStorage) tileEntity).addStackToStorage(itemStack);
            }
            else if (tileEntity instanceof ISidedInventory)
            {
                ISidedInventory inventory = (ISidedInventory) tileEntity;
                int[] slots = inventory.getAccessibleSlotsFromSide(direction.ordinal());
                for (int i = 0; i < slots.length; i++)
                {
                    if (inventory.canInsertItem(slots[i], itemStack, direction.ordinal()))
                    {
                        itemStack = this.addStackToInventory(slots[i], inventory, itemStack);
                    }
                    if (itemStack == null)
                    {
                        return null;
                    }
                }

            }
            else if (tileEntity instanceof IInventory)
            {
                IInventory inventory = (IInventory) tileEntity;

                for (int i = 0; i < inventory.getSizeInventory(); i++)
                {
                    itemStack = this.addStackToInventory(i, inventory, itemStack);
                    if (itemStack == null)
                    {
                        return null;
                    }
                }
            }
        }

        if (itemStack == null || itemStack.stackSize <= 0)
        {
            return null;
        }

        return itemStack;
    }

    public ItemStack addStackToInventory(int slotIndex, IInventory inventory, ItemStack itemStack)
    {
        if (inventory.getSizeInventory() > slotIndex)
        {
            ItemStack stackInInventory = inventory.getStackInSlot(slotIndex);

            if (stackInInventory == null)
            {
                inventory.setInventorySlotContents(slotIndex, itemStack);
                if (inventory.getStackInSlot(slotIndex) == null)
                {
                    return itemStack;
                }
                return null;
            }
            else if (stackInInventory.isItemEqual(itemStack) && stackInInventory.isStackable())
            {
                stackInInventory = stackInInventory.copy();
                int stackLim = Math.min(inventory.getInventoryStackLimit(), itemStack.getMaxStackSize());
                int rejectedAmount = Math.max((stackInInventory.stackSize + itemStack.stackSize) - stackLim, 0);
                stackInInventory.stackSize = Math.min(Math.max((stackInInventory.stackSize + itemStack.stackSize - rejectedAmount), 0), inventory.getInventoryStackLimit());
                itemStack.stackSize = rejectedAmount;
                inventory.setInventorySlotContents(slotIndex, stackInInventory);
            }
        }

        if (itemStack.stackSize <= 0)
        {
            return null;
        }

        return itemStack;
    }

    /** Tries to get an item from a position
     *
     * @param position - location of item
     * @param direction - direction this item is from the original
     * @param ammount - amount up to one stack to grab
     * @return the grabbed item stack */
    public ItemStack tryGrabFromPosition(Vector3 position, ForgeDirection dir, int ammount)
    {
        ItemStack returnStack = null;
        TileEntity tileEntity = position.getTileEntity(world);
        ForgeDirection direction = dir.getOpposite();

        if (tileEntity != null)
        {
            if (tileEntity.getClass() == TileEntityChest.class)
            {
                TileEntityChest[] chests = { (TileEntityChest) tileEntity, null };

                /** Try to find a double chest. */
                for (int i = 2; i < 6; i++)
                {
                    ForgeDirection searchDirection = ForgeDirection.getOrientation(i);
                    Vector3 searchPosition = position.clone();
                    searchPosition.modifyPositionFromSide(searchDirection);

                    if (searchPosition.getTileEntity(world) != null)
                    {
                        if (searchPosition.getTileEntity(world).getClass() == chests[0].getClass())
                        {
                            chests[1] = (TileEntityChest) searchPosition.getTileEntity(world);
                            break;
                        }
                    }
                }

                chestSearch:
                for (TileEntityChest chest : chests)
                {
                    if (chest != null)
                    {
                        for (int i = 0; i < chest.getSizeInventory(); i++)
                        {
                            ItemStack itemStack = this.removeStackFromInventory(i, chest, ammount);

                            if (itemStack != null)
                            {
                                returnStack = itemStack;
                                break chestSearch;
                            }
                        }
                    }
                }
            }
            else if (tileEntity instanceof ISidedInventory)
            {
                ISidedInventory inventory = (ISidedInventory) tileEntity;
                int[] slots = inventory.getAccessibleSlotsFromSide(direction.ordinal());
                for (int i = 0; i < slots.length; i++)
                {
                    int slot = slots[i];
                    ItemStack slotStack = inventory.getStackInSlot(slot);
                    if (inventory.canExtractItem(slot, slotStack, direction.ordinal()))
                    {
                        ItemStack itemStack = this.removeStackFromInventory(slot, inventory, ammount);
                        if (itemStack != null)
                        {
                            returnStack = itemStack;
                            break;
                        }
                    }
                }
            }
            else if (tileEntity instanceof IInventory)
            {
                IInventory inventory = (IInventory) tileEntity;

                for (int i = 0; i < inventory.getSizeInventory(); i++)
                {
                    ItemStack itemStack = this.removeStackFromInventory(i, inventory, ammount);
                    if (itemStack != null)
                    {
                        returnStack = itemStack;
                        break;
                    }
                }
            }
        }

        return returnStack;
    }

    /** Takes an item from the given inventory */
    public ItemStack removeStackFromInventory(int slotIndex, IInventory inventory, int amount)
    {
        if (inventory.getStackInSlot(slotIndex) != null)
        {
            ItemStack itemStack = inventory.getStackInSlot(slotIndex).copy();

            if (this.getFilters().size() == 0 || this.isFiltering(itemStack))
            {
                amount = Math.min(amount, itemStack.stackSize);
                itemStack.stackSize = amount;
                inventory.decrStackSize(slotIndex, amount);
                return itemStack;
            }
        }

        return null;
    }

    /** is the item being restricted to a filter set */
    public boolean isFiltering(ItemStack itemStack)
    {
        if (this.getFilters() != null && itemStack != null)
        {
            for (int i = 0; i < getFilters().size(); i++)
            {
                if (getFilters().get(i) != null)
                {
                    if (getFilters().get(i).isItemEqual(itemStack))
                    {
                        return !inverted;
                    }
                }
            }
        }

        return inverted;
    }

    public List<ItemStack> getFilters()
    {
        if (this.filteredItems == null)
        {
            this.filteredItems = new ArrayList<ItemStack>();
        }
        return this.filteredItems;
    }

}

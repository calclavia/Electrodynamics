package resonantinduction.core.prefab.part;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import calclavia.lib.utility.inventory.ExternalInventory;
import calclavia.lib.utility.inventory.IExternalInventory;
import calclavia.lib.utility.inventory.IExternalInventoryBox;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;

/** Panel based part that provides a prefab for other tiles to build an inventory from. Inventory
 * code is wrapped to an ExternalInventory object allowing inventories to be implemented without
 * repeat copying of code. As well Code is based on TileInventory and should operate in the exact
 * same way.
 * 
 * @author Darkguardsman */
public abstract class PartInventoryPanel extends PartFace implements IExternalInventory, ISidedInventory
{
    protected IExternalInventoryBox inventory;
    protected int maxSlots = 1;
    protected boolean markedForUpdate = false;

    @Override
    public void update()
    {
        super.update();
        if (ticks % 3 == 0 && !world().isRemote && markedForUpdate)
        {
            sendDescUpdate();
        }
    }

    @Override
    public void onInventoryChanged()
    {
        markedForUpdate = true;
    }

    /** Save and load */
    @Override
    public void load(NBTTagCompound nbt)
    {
        super.load(nbt);
        this.getInventory().load(nbt);
    }

    @Override
    public void save(NBTTagCompound nbt)
    {
        super.save(nbt);
        this.getInventory().save(nbt);
    }

    @Override
    public void readDesc(MCDataInput packet)
    {
        super.readDesc(packet);
        getInventory().load(packet.readNBTTagCompound());
    }

    @Override
    public void writeDesc(MCDataOutput packet)
    {
        super.writeDesc(packet);
        NBTTagCompound nbt = new NBTTagCompound();
        getInventory().save(nbt);
        packet.writeNBTTagCompound(nbt);
    }

    @Override
    public IExternalInventoryBox getInventory()
    {
        if (inventory == null)
        {
            inventory = new ExternalInventory(null, this, this.maxSlots);
        }

        return inventory;
    }

    @Override
    public int getSizeInventory()
    {
        return this.getInventory().getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int i)
    {
        return this.getInventory().getStackInSlot(i);
    }

    @Override
    public ItemStack decrStackSize(int i, int j)
    {
        return this.getInventory().decrStackSize(i, j);
    }

    public void incrStackSize(int slot, ItemStack itemStack)
    {
        if (this.getStackInSlot(slot) == null)
        {
            setInventorySlotContents(slot, itemStack.copy());
        }
        else if (this.getStackInSlot(slot).isItemEqual(itemStack))
        {
            getStackInSlot(slot).stackSize += itemStack.stackSize;
        }

        onInventoryChanged();
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i)
    {
        return this.getInventory().getStackInSlotOnClosing(i);
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack)
    {
        this.getInventory().setInventorySlotContents(i, itemstack);

    }

    @Override
    public String getInvName()
    {
        return "Inventory";
    }

    @Override
    public boolean isInvNameLocalized()
    {
        return false;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return this.getInventory().getInventoryStackLimit();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer)
    {
        return this.getInventory().isUseableByPlayer(entityplayer);
    }

    @Override
    public void openChest()
    {
        this.getInventory().openChest();
    }

    @Override
    public void closeChest()
    {
        this.getInventory().closeChest();
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        return this.getInventory().isItemValidForSlot(i, itemstack);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int var1)
    {
        return this.getInventory().getAccessibleSlotsFromSide(var1);
    }

    @Override
    public boolean canInsertItem(int i, ItemStack itemstack, int j)
    {
        return this.getInventory().canInsertItem(i, itemstack, j);
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemstack, int j)
    {
        return this.getInventory().canExtractItem(i, itemstack, j);
    }

    @Override
    public boolean canRemove(ItemStack stack, int slot, ForgeDirection side)
    {
        return slot < this.getSizeInventory();
    }

    @Override
    public boolean canStore(ItemStack stack, int slot, ForgeDirection side)
    {
        return false;
    }
}

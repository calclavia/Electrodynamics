package resonantinduction.archaic.crate;

import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import resonant.api.IExtendedStorage;
import resonant.api.IFilterable;
import resonant.api.IRemovable.ISneakPickup;
import resonant.lib.network.IPacketReceiver;
import resonant.lib.network.PacketHandler;
import resonant.lib.prefab.tile.TileExternalInventory;
import resonantinduction.archaic.Archaic;
import resonantinduction.core.ResonantInduction;

import com.google.common.io.ByteArrayDataInput;

/** Basic single stack inventory.
 * <p/>
 * TODO: Add filter-locking feature. Put filter in, locks the crate to only use that item.
 * 
 * @author DarkGuardsman */
public class TileCrate extends TileExternalInventory implements IPacketReceiver, IExtendedStorage, IFilterable, ISneakPickup
{
    /** max meta size of the crate */
    public static final int maxSize = 2;

    /** delay from last click */
    public long prevClickTime = -1000;

    /** Check to see if oreName items can be force stacked */
    public boolean oreFilterEnabled = false;

    /** Collective total stack of all inv slots */
    private ItemStack sampleStack;
    private ItemStack filterStack;

    private long updateTick = 1;
    private boolean doUpdate = false;

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        if (!worldObj.isRemote)
        {
            this.writeToNBT(new NBTTagCompound());
            if (ticks % updateTick == 0)
            {
                updateTick = 5 + worldObj.rand.nextInt(50);
                doUpdate = true;
            }
            if (doUpdate)
            {
                doUpdate = false;
                PacketHandler.sendPacketToClients(getDescriptionPacket(), this.worldObj);
            }
        }

    }

    /** Gets the slot count for the crate meta */
    public static int getSlotCount(int metadata)
    {
        if (metadata >= 2)
        {
            return 256;
        }
        else if (metadata >= 1)
        {
            return 64;
        }
        return 32;
    }

    @Override
    public InventoryCrate getInventory()
    {
        if (this.inventory == null)
        {
            inventory = new InventoryCrate(this);
        }
        return (InventoryCrate) this.inventory;
    }

    /** Gets the sample stack that represent the total inventory */
    public ItemStack getSampleStack()
    {
        if (this.sampleStack == null)
        {
            this.buildSampleStack();
        }
        return this.sampleStack;
    }

    /** Builds the sample stack using the inventory as a point of reference. Assumes all items match
     * each other, and only takes into account stack sizes */
    public void buildSampleStack()
    {
        buildSampleStack(true);
    }

    public void buildSampleStack(boolean buildInv)
    {
        if (worldObj == null || !worldObj.isRemote)
        {
            ItemStack newSampleStack = null;
            boolean rebuildBase = false;

            /* Creates the sample stack that is used as a collective itemstack */
            for (int slot = 0; slot < this.getSizeInventory(); slot++)
            {
                ItemStack slotStack = this.getInventory().getContainedItems()[slot];
                if (slotStack != null && Item.itemsList[slotStack.itemID] != null && slotStack.stackSize > 0)
                {
                    if (newSampleStack == null)
                    {
                        newSampleStack = slotStack.copy();
                    }
                    else
                    {
                        newSampleStack.stackSize += slotStack.stackSize;
                    }

                    if (slotStack.stackSize > slotStack.getMaxStackSize())
                    {
                        rebuildBase = true;
                    }
                }
            }
            if (newSampleStack == null || newSampleStack.itemID == 0 || newSampleStack.stackSize <= 0)
            {
                this.sampleStack = this.getFilter() != null ? this.getFilter().copy() : null;
            }
            else
            {
                this.sampleStack = newSampleStack.copy();
            }

            /* Rebuild inventory if the inventory is not valid */
            if (buildInv && this.sampleStack != null && (rebuildBase || this.getInventory().getContainedItems().length > this.getSizeInventory()))
            {
                this.getInventory().buildInventory(this.sampleStack);
            }
        }
    }

    @Override
    public ItemStack addStackToStorage(ItemStack stack)
    {
        return BlockCrate.addStackToCrate(this, stack);
    }

    /** Adds an item to the stack */
    public void addToStack(ItemStack stack, int amount)
    {
        if (stack != null)
        {
            ItemStack newStack = stack.copy();
            newStack.stackSize = amount;
            this.addToStack(newStack);
        }
    }

    /** Adds the stack to the sample stack */
    public void addToStack(ItemStack stack)
    {
        if (stack != null && stack.stackSize > 0)
        {
            if (this.getSampleStack() == null)
            {
                this.sampleStack = stack;
                getInventory().buildInventory(getSampleStack());
            }
            else if (this.getSampleStack().isItemEqual(stack) || (this.oreFilterEnabled && OreDictionary.getOreID(getSampleStack()) == OreDictionary.getOreID(stack)))
            {
                getSampleStack().stackSize += stack.stackSize;
                getInventory().buildInventory(getSampleStack());
            }
        }
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount)
    {
        if (sampleStack != null)
        {
            ItemStack var3;

            if (sampleStack.stackSize <= amount)
            {
                var3 = sampleStack;
                sampleStack = null;
                this.onInventoryChanged();
                getInventory().buildInventory(getSampleStack());
                return var3;
            }
            else
            {
                var3 = sampleStack.splitStack(amount);

                if (sampleStack.stackSize == 0)
                {
                    sampleStack = null;
                }

                getInventory().buildInventory(getSampleStack());
                onInventoryChanged();
                return var3;
            }
        }
        else
        {
            return null;
        }
    }

    @Override
    public void onInventoryChanged()
    {
        super.onInventoryChanged();
        if (worldObj != null && !worldObj.isRemote)
            doUpdate = true;
    }

    @Override
    public boolean canStore(ItemStack stack, int slot, ForgeDirection side)
    {
        return getSampleStack() == null || stack != null && (stack.isItemEqual(getSampleStack()) || (this.oreFilterEnabled && OreDictionary.getOreID(getSampleStack()) == OreDictionary.getOreID(stack)));
    }

    /** Gets the current slot count for the crate */
    public int getSlotCount()
    {
        if (this.worldObj == null)
        {
            return TileCrate.getSlotCount(TileCrate.maxSize);
        }
        return TileCrate.getSlotCount(this.getBlockMetadata());
    }

    @Override
    public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
    {
        if (this.worldObj.isRemote)
        {
            try
            {
                if (data.readBoolean())
                {
                    this.sampleStack = ItemStack.loadItemStackFromNBT(PacketHandler.readNBTTagCompound(data));
                    this.sampleStack.stackSize = data.readInt();
                }
                else
                {
                    this.sampleStack = null;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Packet getDescriptionPacket()
    {
        this.buildSampleStack();
        ItemStack stack = this.getSampleStack();
        if (stack != null)
        {
            return ResonantInduction.PACKET_TILE.getPacket(this, true, stack.writeToNBT(new NBTTagCompound()), stack.stackSize);
        }
        else
        {
            return ResonantInduction.PACKET_TILE.getPacket(this, false);
        }
    }

    /** NBT Data */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        /* Load current two inv methods */
        ItemStack stack = null;
        int count = nbt.getInteger("Count");
        if (nbt.hasKey("itemID"))
        {
            stack = new ItemStack(nbt.getInteger("itemID"), count, nbt.getInteger("itemMeta"));
        }
        else
        {
            stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"));
            if (stack != null)
            {
                stack.stackSize = count;
            }
        }

        /* Only load sample stack if the read stack is valid */
        if (stack != null && stack.itemID != 0 && stack.stackSize > 0)
        {
            this.sampleStack = stack;
            this.getInventory().buildInventory(this.sampleStack);
        }
        this.oreFilterEnabled = nbt.getBoolean("oreFilter");
        if (nbt.hasKey("filter"))
        {
            filterStack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("filter"));
        }

    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        /* Re-Build sample stack for saving */
        this.buildSampleStack(false);
        ItemStack stack = this.getSampleStack();
        /* Save sample stack */
        if (stack != null)
        {
            nbt.setInteger("Count", stack.stackSize);
            nbt.setCompoundTag("stack", stack.writeToNBT(new NBTTagCompound()));
        }
        nbt.setBoolean("oreFilter", this.oreFilterEnabled);
        if (this.filterStack != null)
        {
            nbt.setCompoundTag("filter", filterStack.writeToNBT(new NBTTagCompound()));
        }
    }

    @Override
    public ItemStack getFilter()
    {
        return this.filterStack;
    }

    @Override
    public void setFilter(ItemStack filter)
    {
        this.filterStack = filter;
        this.onInventoryChanged();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ItemStack> getRemovedItems(EntityPlayer entity)
    {
        ItemStack sampleStack = getSampleStack();
        ItemStack drop = new ItemStack(Archaic.blockCrate, 1, this.getBlockMetadata());
        if (sampleStack != null && sampleStack.stackSize > 0)
        {
            ItemBlockCrate.setContainingItemStack(drop, sampleStack);
        }
        return Arrays.asList(new ItemStack[] { drop });
    }

}

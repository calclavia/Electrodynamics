package dark.api.reciepes;

import net.minecraft.item.ItemStack;

/** Simple interface that allows an item to control how its salvaged, processed, or refined by a
 * processor. This is 100% optional as the processor by default can break down most items. The only
 * reason to use this is for more complex processing or were the item was created with NBT.
 * 
 * @author Darkgaurdsman */
public interface IProcessable
{
    /** Can this item be Processed by the machine */
    public boolean canProcess(ProcessorType type, ItemStack stack);

    /** Gets the output array of items when this item is processed by a processor machine
     * 
     * @param type - type of machine see ProcessorTypes enum for info
     * @param stack - ItemStack of this item or block
     * @return Array of all item outputed, Make sure to return less than or equal to the amount of
     * items it takes to craft only one of this item */
    public ItemStack[] getProcesserOutput(ProcessorType type, ItemStack stack);
}

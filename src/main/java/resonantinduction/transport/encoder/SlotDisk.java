package resonantinduction.transport.encoder;

import resonantinduction.core.recipe.RecipeLoader;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotDisk extends Slot
{

    public SlotDisk(IInventory par1iInventory, int par2, int par3, int par4)
    {
        super(par1iInventory, par2, par3, par4);
    }

    @Override
    public boolean isItemValid(ItemStack itemStack)
    {
        return itemStack.itemID == RecipeLoader.itemDisk.itemID;
    }

}

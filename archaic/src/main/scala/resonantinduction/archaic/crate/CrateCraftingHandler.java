package resonantinduction.archaic.crate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import resonantinduction.archaic.Archaic;
import cpw.mods.fml.common.ICraftingHandler;

/** Crafting handler for crates
 * 
 * @author Darkguardsman */
public class CrateCraftingHandler implements ICraftingHandler
{
    @Override
    public void onCrafting(EntityPlayer player, ItemStack item, IInventory craftMatrix)
    {
        if (item != null && item.itemID == Archaic.blockCrate.blockID)
        {
            ItemStack centerStack = craftMatrix.getStackInSlot(4);
            if (centerStack != null && centerStack.itemID == Archaic.blockCrate.blockID)
            {
                ItemStack containedStack = ItemBlockCrate.getContainingItemStack(centerStack);
                if (centerStack != null)
                {
                    ItemBlockCrate.setContainingItemStack(item, containedStack);
                }
            }
        }

    }

    @Override
    public void onSmelting(EntityPlayer player, ItemStack item)
    {
        // TODO Auto-generated method stub

    }

}

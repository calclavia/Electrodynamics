package resonantinduction.archaic.crate;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.ItemStack;
import resonantinduction.archaic.ArchaicBlocks;
import net.minecraftforge.oredict.ShapedOreRecipe;

/** Crafting handler for crates
 * 
 * @author Darkguardsman */
public class CrateRecipe extends ShapedOreRecipe implements IRecipe
{
    public CrateRecipe(Block result, Object... recipe){ super(new ItemStack(result), recipe); }
    public CrateRecipe(Item result, Object... recipe){ super(new ItemStack(result), recipe); }
    public CrateRecipe(ItemStack result, Object... recipe){super(result, recipe);}

    @Override
    public ItemStack getCraftingResult(InventoryCrafting grid)
    {
        ItemStack result = super.getCraftingResult(grid);
        Item crateItem = Item.getItemFromBlock(ArchaicBlocks.blockCrate());

        if (result != null && result.getItem() == crateItem )
        {
            ItemStack centerStack = grid.getStackInSlot(4);
            if (centerStack != null && centerStack.getItem() == crateItem )
            {
                ItemStack containedStack = ItemBlockCrate.getContainingItemStack(centerStack);
                if (centerStack != null)
                {
                    ItemBlockCrate.setContainingItemStack(result, containedStack);
                }
            }
        }
        return result;
    }
}

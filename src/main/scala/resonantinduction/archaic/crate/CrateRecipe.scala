package resonantinduction.archaic.crate

import net.minecraft.block.Block
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraftforge.oredict.ShapedOreRecipe
import resonantinduction.archaic.ArchaicContent

/**
 * Crafting handler for crates
 *
 * @author Darkguardsman
 */
class CrateRecipe(result: ItemStack, recipe: AnyRef*) extends ShapedOreRecipe(result, recipe) with IRecipe
{
    def this(result: Block, recipe: AnyRef*)
    {
        this(new ItemStack(result), recipe)
    }

    def this(result: Item, recipe: AnyRef*)
    {
        this(new ItemStack(result), recipe)
    }

    override def getCraftingResult(grid: InventoryCrafting): ItemStack =
    {
        val result: ItemStack = super.getCraftingResult(grid)
        val crateItem: Item = Item.getItemFromBlock(ArchaicContent.blockCrate)
        if (result != null && result.getItem == crateItem)
        {
            val centerStack: ItemStack = grid.getStackInSlot(4)
            if (centerStack != null && centerStack.getItem == crateItem)
            {
                val containedStack: ItemStack = ItemBlockCrate.getContainingItemStack(centerStack)
                if (centerStack != null)
                {
                    ItemBlockCrate.setContainingItemStack(result, containedStack)
                }
            }
        }
        return result
    }
}
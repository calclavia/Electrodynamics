package resonantinduction.core

import net.minecraft.block.Block
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.ItemStack
import resonant.content.loader.ContentHolder
import resonantinduction.core.content.BlockDecoration

/**
 * The core contents of Resonant Induction
 * @author Calclavia
 */
object CoreContent extends ContentHolder
{
  val decoration: Block = new BlockDecoration()

  manager.setTab(ResonantTab).setPrefix(Reference.prefix)

  /**
   * Recipe registration
   */
  override def postInit()
  {
    recipes += shaped(new ItemStack(decoration, 8, 3), "XXX", "XCX", "XXX", 'X', Blocks.cobblestone, 'C', new ItemStack(Items.coal, 1, 1))
    recipes +=(new ItemStack(decoration, 3), new ItemStack(decoration, 1, 5), 5)
    recipes +=(decoration, new ItemStack(decoration, 1, 4), 5)
    recipes += shaped(new ItemStack(decoration, 8, 7), "XXX", "XVX", "XXX", 'X', new ItemStack(decoration), 'V', Blocks.vine)
    recipes += shaped(new ItemStack(decoration, 4), "XX ", "XX ", "   ", 'X', new ItemStack(decoration, 1, 5))
    recipes += shaped(new ItemStack(decoration, 4, 1), "XXX", "XXX", "XX ", 'X', Blocks.stone_slab)
    recipes += shaped(new ItemStack(decoration, 8, 2), "XXX", "X X", "XXX", 'X', new ItemStack(decoration, 1, 5))
    recipes += shaped(new ItemStack(decoration, 5, 10), "IXI", "XXX", "IXI", 'X', new ItemStack(decoration, 1, 5), 'I', Items.iron_ingot)
  }
}

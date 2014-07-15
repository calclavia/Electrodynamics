package resonantinduction.core

import java.util.HashMap

import net.minecraft.block.Block
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.fluids.BlockFluidFinite
import resonant.content.loader.{ContentHolder, ExplicitContentName}
import resonant.lib.ore.OreGenerator
import resonantinduction.core.content.BlockDecoration
import resonantinduction.core.resource.fluid.ItemResourceBucket
import resonantinduction.core.resource.{ItemResourceDust, TileDust}

/**
 * The core contents of Resonant Induction
 * @author Calclavia
 */
object CoreContent extends ContentHolder
{
  final val blockMixtureFluids: HashMap[Integer, BlockFluidFinite] = new HashMap[Integer, BlockFluidFinite]
  final val blockMoltenFluid: HashMap[Integer, BlockFluidFinite] = new HashMap[Integer, BlockFluidFinite]

  /**
   * Blocks
   */
  var blockOreCopper: Block = null
  var blockOreTin: Block = null

  /**
   * Items
   */
  var itemMotor: Item = null
  var itemCircuitBasic: Item = null
  var itemCircuitAdvanced: Item = null
  var itemCircuitElite: Item = null

  var itemPlateCopper: Item = null
  var itemPlateTin: Item = null
  var itemPlateBronze: Item = null
  var itemPlateSteel: Item = null
  var itemPlateIron: Item = null
  var itemPlateGold: Item = null

  var itemIngotSteel: Item = null
  var itemIngotBronze: Item = null
  var itemDustSteel: Item = null
  var itemDustBronze: Item = null

  var generationOreCopper: OreGenerator = null
  var generationOreTin: OreGenerator = null

  val decoration: Block = new BlockDecoration()
  @ExplicitContentName("dust")
  val blockDust: Block = new TileDust().setCreativeTab(null)
  @ExplicitContentName("refinedDust")
  val blockRefinedDust: Block = new TileDust().setCreativeTab(null)

  /**
   * Items
   */
  @ExplicitContentName
  val rubble = new ItemResourceDust
  @ExplicitContentName
  val dust = new ItemResourceDust
  @ExplicitContentName
  val refinedDust = new ItemResourceDust
  @ExplicitContentName
  val bucketMixture = new ItemResourceBucket(false)
  @ExplicitContentName
  val bucketMolten = new ItemResourceBucket(true)

  manager.setTab(ResonantTab).setPrefix(Reference.prefix)

  /**
   * Recipe registration
   */
  override def postInit()
  {
    /**
     * Resources
     */


    /**
     * Decoration
     */
    recipes += shaped(new ItemStack(decoration, 8, 3), "XXX", "XCX", "XXX", 'X', Blocks.cobblestone, 'C', new ItemStack(Items.coal, 1, 1))
    recipes += (new ItemStack(decoration, 3), new ItemStack(decoration, 1, 5), 5)
    recipes += (decoration, new ItemStack(decoration, 1, 4), 5)
    recipes += shaped(new ItemStack(decoration, 8, 7), "XXX", "XVX", "XXX", 'X', new ItemStack(decoration), 'V', Blocks.vine)
    recipes += shaped(new ItemStack(decoration, 4), "XX ", "XX ", "   ", 'X', new ItemStack(decoration, 1, 5))
    recipes += shaped(new ItemStack(decoration, 4, 1), "XXX", "XXX", "XX ", 'X', Blocks.stone_slab)
    recipes += shaped(new ItemStack(decoration, 8, 2), "XXX", "X X", "XXX", 'X', new ItemStack(decoration, 1, 5))
    recipes += shaped(new ItemStack(decoration, 5, 10), "IXI", "XXX", "IXI", 'X', new ItemStack(decoration, 1, 5), 'I', Items.iron_ingot)
  }
}

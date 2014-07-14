package resonantinduction.core

import java.util.HashMap

import net.minecraft.block.Block
import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.fluids.BlockFluidFinite
import resonant.content.loader.{ContentHolder, ExplicitContentName}
import resonantinduction.core.content.BlockIndustrialStone
import resonantinduction.core.resource.fluid.ItemOreResourceBucket
import resonantinduction.core.resource.{ItemOreResource, TileDust}

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
  var itemIngotCopper: Item = null
  var itemIngotTin: Item = null
  var itemIngotSteel: Item = null
  var itemIngotBronze: Item = null
  var itemDustSteel: Item = null
  var itemDustBronze: Item = null
  var generationOreCopper: Nothing = null
  var generationOreTin: Nothing = null

  val decoration: Block = new BlockIndustrialStone()
  @ExplicitContentName("dust")
  val blockDust: Block = new TileDust().setCreativeTab(null)
  @ExplicitContentName("refinedDust")
  val blockRefinedDust: Block = new TileDust().setCreativeTab(null)

  /**
   * Items
   */
  @ExplicitContentName
  val rubble = new ItemOreResource
  @ExplicitContentName
  val dust = new ItemOreResource
  @ExplicitContentName
  val refinedDust = new ItemOreResource
  @ExplicitContentName
  val bucketMixture = new ItemOreResourceBucket(false)
  @ExplicitContentName
  val bucketMolten = new ItemOreResourceBucket(true)

  /*
  val itemBiomass = contentRegistry.createItem(classOf[ItemBiomass])
  val itemDevStaff = contentRegistry.createItem(classOf[ItemDevStaff])
  val  itemFlour = contentRegistry.createItem(classOf[ItemFlour])
  */

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
    recipes += shaped(new ItemStack(decoration, 8, 3), Array[AnyRef]("XXX", "XCX", "XXX", 'X', Block.cobblestone, 'C', new ItemStack(Item.coal, 1, 1)))
    recipes +=(decoration, 3, new ItemStack(decoration, 1, 5), 5)
    recipes +=(decoration, new ItemStack(decoration, 1, 4), 5)
    recipes += shaped(new ItemStack(decoration, 8, 7), Array[AnyRef]("XXX", "XVX", "XXX", 'X', new ItemStack(decoration), 'V', Block.vine))
    recipes += shaped(new ItemStack(decoration, 4), Array[AnyRef]("XX ", "XX ", "   ", 'X', new ItemStack(decoration, 1, 5)))
    recipes += shaped(new ItemStack(decoration, 4, 1), Array[AnyRef]("XXX", "XXX", "XX ", 'X', Block.stoneSingleSlab))
    recipes += shaped(new ItemStack(decoration, 8, 2), Array[AnyRef]("XXX", "X X", "XXX", 'X', new ItemStack(decoration, 1, 5)))
    recipes += shaped(new ItemStack(decoration, 5, 10), Array[AnyRef]("IXI", "XXX", "IXI", 'X', new ItemStack(decoration, 1, 5), 'I', Item.ingotIron))
  }
}

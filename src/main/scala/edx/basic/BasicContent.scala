package edx.basic

import edx.basic.blocks.{ItemImprint, TileImprinter, TileTurntable}
import edx.basic.engineering.ItemHammer
import edx.basic.firebox.{TileFirebox, TileHotPlate}
import edx.basic.fluid.grate.TileGrate
import edx.basic.fluid.gutter.TileGutter
import edx.basic.fluid.tank.TileTank
import edx.basic.process.mixing.TileGlassJar
import edx.basic.process.{TileCastingMold, TileSieve, TileWorkbench}
import edx.core.resource.content.{ItemAlloyDust, TileDust}
import edx.core.{EDXCreativeTab, Reference}
import edx.mechanical.mech.gear.ItemHandCrank
import net.minecraft.block.Block
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import resonant.lib.mod.content.ContentHolder
import resonant.lib.utility.recipe.UniversalRecipe
;

object BasicContent extends ContentHolder
{
  /**
   * Resources
   */
  var itemAlloyDust: Item = new ItemAlloyDust
  var blockDust: Block = new TileDust

  var itemImprint: Item = new ItemImprint
  var itemHammer: Item = new ItemHammer
  var itemHandCrank: Item = new ItemHandCrank

  //  var blockEngineeringTable: Block = new TileEngineeringTable
  //manager.newBlock(classOf[BlockCrate])
  var blockImprinter: Block = new TileImprinter
  var blockTurntable: Block = new TileTurntable
  var blockFirebox: Block = new TileFirebox
  var blockHotPlate: Block = new TileHotPlate
  var blockSieve: Block = new TileSieve
  var blockCast: Block = new TileCastingMold
  var blockGutter: Block = new TileGutter
  var blockTank: Block = new TileTank
  var blockWorkbench: Block = new TileWorkbench
  var blockGlassJar: Block = new TileGlassJar

  //var blockFilter: Block = contentRegistry.newBlock( classOf[ TileFilter ] )
  var blockGrate: Block = new TileGrate

  //Constructor
  manager.setTab(EDXCreativeTab)
  manager.setPrefix(Reference.prefix)

  override def postInit()
  {
    //recipes += shaped(ArchaicContent.blockEngineeringTable, "P", "C", 'P', Blocks.wooden_pressure_plate, 'C', Blocks.crafting_table)
    //recipes += shaped(ArchaicBlocks.blockFilter, "B", "P", "B", 'B', Blocks.iron_bars, 'P', Items.paper)

    //recipes += shaped(new ItemStack(ArchaicBlocks.blockCrate, 1, 0), "WWW", "WSW", "WWW", 'S', "stickWood", 'W', "logWood")
    //recipes += new CrateRecipe(new ItemStack(ArchaicBlocks.blockCrate, 1, 1), "WWW", "WSW", "WWW", 'S', new ItemStack(ArchaicBlocks.blockCrate, 1, 0), 'W', "ingotIron")
    //recipes +=new CrateRecipe(new ItemStack(ArchaicBlocks.blockCrate, 1, 2), "WWW", "WSW", "WWW", 'S', new ItemStack(ArchaicBlocks.blockCrate, 1, 1), 'W', UniversalRecipe.PRIMARY_METAL.get)

    recipes += shaped(BasicContent.blockFirebox, "III", "SFS", "SSS", 'I', Items.iron_ingot, 'F', Blocks.furnace, 'S', Blocks.stone)
    recipes += shaped(new ItemStack(BasicContent.blockFirebox, 1, 1), "III", "SFS", "SSS", 'I', UniversalRecipe.PRIMARY_METAL.get, 'F', new ItemStack(BasicContent.blockFirebox, 1, 0), 'S', UniversalRecipe.WIRE.get)
    recipes += shaped(BasicContent.blockImprinter, "SSS", "W W", "PPP", 'S', Blocks.stone, 'P', Blocks.piston, 'W', "logWood")
    recipes += shaped(BasicContent.blockTurntable, "SSS", "PGP", "WWW", 'S', Blocks.stone, 'G', Items.redstone, 'P', Blocks.piston, 'W', "logWood")
    recipes += shaped(BasicContent.blockCast, "I I", "IBI", "III", 'S', Items.iron_ingot, 'B', Blocks.iron_bars)
    recipes += shaped(BasicContent.blockGutter, "S S", "I I", "III", 'S', Items.stick, 'I', "cobblestone")
    recipes += shaped(BasicContent.blockGrate, "WBW", "B B", "WBW", 'B', Blocks.iron_bars, 'W', "plankWood")
    recipes += shaped(BasicContent.blockHotPlate, "SSS", "III", 'I', Items.iron_ingot, 'S', Blocks.stone)
    recipes += shaped(BasicContent.blockSieve, "SPS", "SAS", "SSS", 'P', Blocks.piston, 'A', Items.stone_pickaxe, 'S', Blocks.stone)
    recipes += shaped(BasicContent.blockTank, "GGG", "GSG", "GGG", 'G', Blocks.glass, 'S', Items.iron_ingot)

    recipes += shaped(itemHandCrank, "S  ", "SSS", "  S", 'S', "stickWood")
    recipes += shaped(itemImprint, "PPP", "PIP", "PPP", 'P', Items.paper, 'I', new ItemStack(Items.dye, 0))
    recipes += shaped(itemHammer, "CC ", "CS ", "  S", 'C', "cobblestone", 'S', "stickWood")
  }
}
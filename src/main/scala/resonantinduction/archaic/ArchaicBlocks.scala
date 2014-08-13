package resonantinduction.archaic

import net.minecraft.block.Block
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.ItemStack
import resonant.content.loader.ContentHolder
import resonant.lib.recipe.UniversalRecipe
;

object ArchaicBlocks extends ContentHolder {

  var blockEngineeringTable: Block = _
  var blockCrate: Block = _
  var blockImprinter: Block = _
  var blockTurntable: Block = _
  var blockFirebox: Block = _
  var blockHotPlate: Block = _
  var blockMillstone: Block = _
  var blockCast: Block = _
  var blockFilter: Block = _
  var blockGrate: Block = _
  var blockGutter: Block = _
  var blockTank: Block = _

  override def postInit() {
    recipes += shaped(ArchaicBlocks.blockEngineeringTable, "P", "C", 'P', Blocks.wooden_pressure_plate, 'C', Blocks.crafting_table)
    //recipes += shaped(ArchaicBlocks.blockFilter, "B", "P", "B", 'B', Blocks.iron_bars, 'P', Items.paper)

    //recipes += shaped(new ItemStack(ArchaicBlocks.blockCrate, 1, 0), "WWW", "WSW", "WWW", 'S', "stickWood", 'W', "logWood")
    //recipes += new CrateRecipe(new ItemStack(ArchaicBlocks.blockCrate, 1, 1), "WWW", "WSW", "WWW", 'S', new ItemStack(ArchaicBlocks.blockCrate, 1, 0), 'W', "ingotIron")
    //recipes +=new CrateRecipe(new ItemStack(ArchaicBlocks.blockCrate, 1, 2), "WWW", "WSW", "WWW", 'S', new ItemStack(ArchaicBlocks.blockCrate, 1, 1), 'W', UniversalRecipe.PRIMARY_METAL.get)

    recipes += shaped(ArchaicBlocks.blockFirebox, "III", "SFS", "SSS", 'I', Items.iron_ingot, 'F', Blocks.furnace, 'S', Blocks.stone)
    recipes += shaped(new ItemStack(ArchaicBlocks.blockFirebox, 1, 1), "III", "SFS", "SSS", 'I', UniversalRecipe.PRIMARY_METAL.get, 'F', new ItemStack(ArchaicBlocks.blockFirebox, 1, 0), 'S', UniversalRecipe.WIRE.get)
    recipes += shaped(ArchaicBlocks.blockImprinter, "SSS", "W W", "PPP", 'S', Blocks.stone, 'P', Blocks.piston, 'W', "logWood")
    recipes += shaped(ArchaicBlocks.blockTurntable, "SSS", "PGP", "WWW", 'S', Blocks.stone, 'G', Items.redstone, 'P', Blocks.piston, 'W', "logWood")
    recipes += shaped(ArchaicBlocks.blockCast, "I I", "IBI", "III", 'S', Items.iron_ingot, 'B', Blocks.iron_bars)
    recipes += shaped(ArchaicBlocks.blockGutter, "S S", "I I", "III", 'S', Items.stick, 'I', "cobblestone")
    recipes += shaped(ArchaicBlocks.blockGrate, "WBW", "B B", "WBW", 'B', Blocks.iron_bars, 'W', "plankWood")
    recipes += shaped(ArchaicBlocks.blockHotPlate, "SSS", "III", 'I', Items.iron_ingot, 'S', Blocks.stone)
    recipes += shaped(ArchaicBlocks.blockMillstone, "SPS", "SAS", "SSS", 'P', Blocks.piston, 'A', Items.stone_pickaxe, 'S', Blocks.stone)
    recipes += shaped(ArchaicBlocks.blockTank, "GGG", "GSG", "GGG", 'G', Blocks.glass, 'S', Items.iron_ingot)
  }
}
package com.calclavia.edx.basic

import com.calclavia.edx.basic.blocks.{TileImprinter, TileTurntable, ItemImprint}
import com.calclavia.edx.basic.fluid.grate.TileGrate
import com.calclavia.edx.basic.fluid.gutter.TileGutter
import com.calclavia.edx.basic.fluid.tank.TileTank
import com.calclavia.edx.basic.process.grinding.{ItemHammer, TileWorkbench}
import com.calclavia.edx.basic.process.mixing.TileGlassJar
import com.calclavia.edx.basic.process.sifting.TileSieve
import com.calclavia.edx.basic.process.smelting.TileCastingMold
import com.calclavia.edx.basic.process.smelting.firebox.{TileHotPlate, TileFirebox}
import com.calclavia.edx.mechanical.mech.gear.ItemHandCrank
import edx.basic.blocks.TileImprinter
import edx.basic.process.grinding.ItemHammer
import edx.basic.process.smelting.firebox.TileHotPlate
import edx.core.resource.content.{ItemAlloyDust, TileDust}
import edx.core.{EDXCreativeTab, Reference}
import net.minecraft.block.Block
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import resonantengine.lib.utility.recipe.UniversalRecipe
import resonantengine.prefab.modcontent.ContentHolder
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

  var blockWorkbench: Block = new TileWorkbench
  var blockImprinter: Block = new TileImprinter
  var blockTurntable: Block = new TileTurntable
  var blockFirebox: Block = new TileFirebox
  var blockHotPlate: Block = new TileHotPlate
  var blockSieve: Block = new TileSieve
  var blockCast: Block = new TileCastingMold
  var blockGutter: Block = new TileGutter
  var blockTank: Block = new TileTank
  var blockGlassJar: Block = new TileGlassJar

  var blockGrate: Block = new TileGrate

  //Constructor
  manager.setTab(EDXCreativeTab)
  manager.setPrefix(Reference.prefix)

  override def postInit()
  {
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
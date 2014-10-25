package resonantinduction.archaic

import net.minecraft.block.Block
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.oredict.OreDictionary
import resonant.content.loader.ContentHolder
import resonant.lib.recipe.UniversalRecipe
import resonantinduction.archaic.blocks.{ItemImprint, TileImprinter, TileTurntable}
import resonantinduction.archaic.engineering.{ItemHammer, TileEngineeringTable}
import resonantinduction.archaic.firebox.{TileFirebox, TileHotPlate}
import resonantinduction.archaic.fluid.grate.TileGrate
import resonantinduction.archaic.fluid.gutter.TileGutter
import resonantinduction.archaic.fluid.tank.TileTank
import resonantinduction.archaic.process.{TileCastingMold, TileMillstone}
import resonantinduction.core.{Reference, RICreativeTab}
import resonantinduction.mechanical.mech.gear.ItemHandCrank
;

object ArchaicContent extends ContentHolder
{

    var itemImprint: Item = null
    var itemHammer: Item = null
    var itemHandCrank: Item = null

    var blockEngineeringTable: Block = null
    var blockCrate: Block = null//manager.newBlock(classOf[BlockCrate])
    var blockImprinter: Block = null
    var blockTurntable: Block = null
    var blockFirebox: Block = null
    var blockHotPlate: Block = null
    var blockMillstone: Block = null
    var blockCast: Block = null
    //var blockFilter: Block = contentRegistry.newBlock( classOf[ TileFilter ] )
    var blockGrate: Block = null
    var blockGutter: Block = null
    var blockTank: Block = null

    //Constructor
    manager.setTab(RICreativeTab)
    manager.setPrefix(Reference.prefix)

    override def preInit()
    {
        super.preInit()
        itemImprint = manager.newItem(classOf[ItemImprint])
        itemHammer = manager.newItem(classOf[ItemHammer])
        itemHandCrank = manager.newItem(classOf[ItemHandCrank])

        blockEngineeringTable = manager.newBlock(classOf[TileEngineeringTable])
        blockCrate= null//manager.newBlock(classOf[BlockCrate])
        blockImprinter = manager.newBlock(classOf[TileImprinter])
        blockTurntable = manager.newBlock(classOf[TileTurntable])
        blockFirebox = manager.newBlock(classOf[TileFirebox])
        blockHotPlate = manager.newBlock(classOf[TileHotPlate])
        blockMillstone = manager.newBlock(classOf[TileMillstone])
        blockCast = manager.newBlock(classOf[TileCastingMold])
        //var blockFilter: Block = contentRegistry.newBlock( classOf[ TileFilter ] )
        blockGrate = manager.newBlock(classOf[TileGrate])
        blockGutter = manager.newBlock(classOf[TileGutter])
        blockTank = manager.newBlock(classOf[TileTank])
    }
    override def postInit()
    {
        RICreativeTab.itemStack = new ItemStack(ArchaicContent.blockEngineeringTable)
        if (OreDictionary.getOres("cobblestone") == null)
        {
            OreDictionary.registerOre("cobblestone", Blocks.cobblestone)
        }
        if (OreDictionary.getOres("stickWood") == null)
        {
            OreDictionary.registerOre("stickWood", Items.stick)
        }

        recipes += shaped(ArchaicContent.blockEngineeringTable, "P", "C", 'P', Blocks.wooden_pressure_plate, 'C', Blocks.crafting_table)
        //recipes += shaped(ArchaicBlocks.blockFilter, "B", "P", "B", 'B', Blocks.iron_bars, 'P', Items.paper)

        //recipes += shaped(new ItemStack(ArchaicBlocks.blockCrate, 1, 0), "WWW", "WSW", "WWW", 'S', "stickWood", 'W', "logWood")
        //recipes += new CrateRecipe(new ItemStack(ArchaicBlocks.blockCrate, 1, 1), "WWW", "WSW", "WWW", 'S', new ItemStack(ArchaicBlocks.blockCrate, 1, 0), 'W', "ingotIron")
        //recipes +=new CrateRecipe(new ItemStack(ArchaicBlocks.blockCrate, 1, 2), "WWW", "WSW", "WWW", 'S', new ItemStack(ArchaicBlocks.blockCrate, 1, 1), 'W', UniversalRecipe.PRIMARY_METAL.get)

        recipes += shaped(ArchaicContent.blockFirebox, "III", "SFS", "SSS", 'I', Items.iron_ingot, 'F', Blocks.furnace, 'S', Blocks.stone)
        recipes += shaped(new ItemStack(ArchaicContent.blockFirebox, 1, 1), "III", "SFS", "SSS", 'I', UniversalRecipe.PRIMARY_METAL.get, 'F', new ItemStack(ArchaicContent.blockFirebox, 1, 0), 'S', UniversalRecipe.WIRE.get)
        recipes += shaped(ArchaicContent.blockImprinter, "SSS", "W W", "PPP", 'S', Blocks.stone, 'P', Blocks.piston, 'W', "logWood")
        recipes += shaped(ArchaicContent.blockTurntable, "SSS", "PGP", "WWW", 'S', Blocks.stone, 'G', Items.redstone, 'P', Blocks.piston, 'W', "logWood")
        recipes += shaped(ArchaicContent.blockCast, "I I", "IBI", "III", 'S', Items.iron_ingot, 'B', Blocks.iron_bars)
        recipes += shaped(ArchaicContent.blockGutter, "S S", "I I", "III", 'S', Items.stick, 'I', "cobblestone")
        recipes += shaped(ArchaicContent.blockGrate, "WBW", "B B", "WBW", 'B', Blocks.iron_bars, 'W', "plankWood")
        recipes += shaped(ArchaicContent.blockHotPlate, "SSS", "III", 'I', Items.iron_ingot, 'S', Blocks.stone)
        recipes += shaped(ArchaicContent.blockMillstone, "SPS", "SAS", "SSS", 'P', Blocks.piston, 'A', Items.stone_pickaxe, 'S', Blocks.stone)
        recipes += shaped(ArchaicContent.blockTank, "GGG", "GSG", "GGG", 'G', Blocks.glass, 'S', Items.iron_ingot)

        recipes += shaped(itemHandCrank, "S  ", "SSS", "  S", 'S', "stickWood")
        recipes += shaped(itemImprint, "PPP", "PIP", "PPP", 'P', Items.paper, 'I', new ItemStack(Items.dye, 0))
        recipes += shaped(itemHammer, "CC ", "CS ", "  S", 'C', "cobblestone", 'S', "stickWood")
    }
}
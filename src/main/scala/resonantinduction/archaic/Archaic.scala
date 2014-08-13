package resonantinduction.archaic

import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.common.{Mod, SidedProxy}
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.{OreDictionary, ShapedOreRecipe}
import resonant.content.loader.ModManager
import resonant.lib.recipe.UniversalRecipe
import resonantinduction.archaic.blocks.{ItemImprint, TileImprinter, TileTurntable}
import resonantinduction.archaic.crate.CrateRecipe
import resonantinduction.archaic.engineering.{ItemHammer, TileEngineeringTable}
import resonantinduction.archaic.firebox.{TileFirebox, TileHotPlate}
import resonantinduction.archaic.fluid.grate.TileGrate
import resonantinduction.archaic.fluid.gutter.TileGutter
import resonantinduction.archaic.fluid.tank.TileTank
import resonantinduction.archaic.process.{TileCastingMold, TileMillstone}
import resonantinduction.core.{Reference, ResonantTab, Settings}
import resonantinduction.mechanical.gear.ItemHandCrank
@Mod( modid = Archaic.ID, name = Archaic.NAME, version = Reference.version, modLanguage = "scala" , dependencies = "required-after:" + Reference.coreID)
object Archaic {

    final val ID = "ResonantInduction|Archaic"

    final val NAME = Reference.name + " Archaic"

    val contentRegistry = new ModManager().setPrefix( Reference.prefix ).setTab( ResonantTab )

    var INSTANCE  = this

    @SidedProxy( clientSide = "resonantinduction.archaic.ClientProxy", serverSide = "resonantinduction.archaic.CommonProxy" )
    var proxy : CommonProxy = _

    @EventHandler
    def preInit( evt : FMLPreInitializationEvent ) {
        NetworkRegistry.INSTANCE.registerGuiHandler( this, proxy )
        Settings.config.load()
        ArchaicBlocks.blockEngineeringTable = contentRegistry.newBlock(classOf[ TileEngineeringTable])
        //ArchaicBlocks.blockCrate = contentRegistry.newBlock( classOf[ BlockCrate ], classOf[ ItemBlockCrate ], classOf[ TileCrate ] )
        ArchaicBlocks.blockImprinter = contentRegistry.newBlock(classOf[ TileImprinter ] )
        ArchaicBlocks.blockTurntable = contentRegistry.newBlock( classOf[ TileTurntable ] )
        ArchaicBlocks.blockFirebox = contentRegistry.newBlock(classOf[ TileFirebox ] )
        ArchaicBlocks.blockHotPlate = contentRegistry.newBlock(classOf[ TileHotPlate ] )
        ArchaicBlocks.blockMillstone = contentRegistry.newBlock( classOf[ TileMillstone ] )
        ArchaicBlocks.blockCast = contentRegistry.newBlock( classOf[ TileCastingMold ] )
        ArchaicBlocks.blockGutter = contentRegistry.newBlock( classOf[ TileGutter ] )
        ArchaicBlocks.blockGrate = contentRegistry.newBlock( classOf[ TileGrate ] )
        //ArchaicBlocks.blockFilter = contentRegistry.newBlock( classOf[ TileFilter ] )
        ArchaicBlocks.blockTank = contentRegistry.newBlock( classOf[ TileTank ] )
        ArchaicItems.itemHandCrank = contentRegistry.newItem( classOf[ ItemHandCrank ] )
        ArchaicItems.itemImprint = contentRegistry.newItem( classOf[ ItemImprint ] )
        ArchaicItems.itemHammer = contentRegistry.newItem( classOf[ ItemHammer ] )
        Settings.config.save()
        proxy.preInit()
    }

    @EventHandler
    def init( evt : FMLInitializationEvent ) {
        //Settings.setModMetadata( metadata, ID, NAME, ResonantInduction.ID )
        proxy.init()
    }

    @EventHandler
    def postInit( evt : FMLPostInitializationEvent ) {
        ResonantTab.itemStack = new ItemStack( ArchaicBlocks.blockEngineeringTable )
        if ( OreDictionary.getOres( "cobblestone" ) == null ) {
            OreDictionary.registerOre( "cobblestone", Blocks.cobblestone )
        }
        if ( OreDictionary.getOres( "stickWood" ) == null ) {
            OreDictionary.registerOre( "stickWood", Items.stick )
        }
        GameRegistry.addRecipe( new ShapedOreRecipe( ArchaicBlocks.blockEngineeringTable, Array( "P", "C", 'P', Blocks.wooden_pressure_plate, 'C', Blocks.crafting_table ) ) )
        GameRegistry.addRecipe( new ShapedOreRecipe( ArchaicBlocks.blockFilter, Array( "B", "P", "B", 'B', Blocks.iron_bars, 'P', Items.paper ) ) )
        GameRegistry.addRecipe( new ShapedOreRecipe( new ItemStack( ArchaicBlocks.blockCrate, 1, 0 ), Array( "WWW", "WSW", "WWW",
            'S', "stickWood", 'W', "logWood" ) ) )
        GameRegistry.addRecipe( new CrateRecipe( new ItemStack( ArchaicBlocks.blockCrate, 1, 1 ), Array( "WWW", "WSW", "WWW",
            'S', new ItemStack( ArchaicBlocks.blockCrate, 1, 0 ), 'W', "ingotIron" ) ) )
        GameRegistry.addRecipe( new CrateRecipe( new ItemStack( ArchaicBlocks.blockCrate, 1, 2 ), Array( "WWW", "WSW", "WWW",
            'S', new ItemStack( ArchaicBlocks.blockCrate, 1, 1 ), 'W', UniversalRecipe.PRIMARY_METAL.get ) ) )
        GameRegistry.addRecipe( new ShapedOreRecipe( ArchaicBlocks.blockFirebox, Array( "III", "SFS", "SSS", 'I', Items.iron_ingot,
            'F', Blocks.furnace, 'S', Blocks.stone ) ) )
        GameRegistry.addRecipe( new ShapedOreRecipe( new ItemStack( ArchaicBlocks.blockFirebox, 1, 1 ), Array( "III", "SFS", "SSS",
            'I', UniversalRecipe.PRIMARY_METAL.get, 'F', new ItemStack( ArchaicBlocks.blockFirebox, 1, 0 ), 'S', UniversalRecipe.WIRE.get ) ) )
        GameRegistry.addRecipe( new ShapedOreRecipe( ArchaicBlocks.blockImprinter, Array( "SSS", "W W", "PPP", 'S', Blocks.stone,
            'P', Blocks.piston, 'W', "logWood" ) ) )
        GameRegistry.addRecipe( new ShapedOreRecipe( ArchaicBlocks.blockTurntable, Array( "SSS", "PGP", "WWW", 'S', Blocks.stone,
            'G', Items.redstone, 'P', Blocks.piston, 'W', "logWood" ) ) )
        GameRegistry.addRecipe( new ShapedOreRecipe( ArchaicBlocks.blockCast, Array( "I I", "IBI", "III", 'S', Items.iron_ingot, 'B',
            Blocks.iron_bars ) ) )
        GameRegistry.addRecipe( new ShapedOreRecipe( ArchaicBlocks.blockGutter, Array( "S S", "I I", "III", 'S', Items.stick, 'I',
            "cobblestone" ) ) )
        GameRegistry.addRecipe( new ShapedOreRecipe( ArchaicBlocks.blockGrate, Array( "WBW", "B B", "WBW", 'B', Blocks.iron_bars,
            'W', "plankWood" ) ) )
        GameRegistry.addRecipe( new ShapedOreRecipe( ArchaicBlocks.blockHotPlate, Array( "SSS", "III", 'I', Items.iron_ingot, 'S',
            Blocks.stone ) ) )
        GameRegistry.addRecipe( new ShapedOreRecipe( ArchaicBlocks.blockMillstone, Array( "SPS", "SAS", "SSS", 'P', Blocks.piston,
            'A', Items.stone_pickaxe, 'S', Blocks.stone ) ) )
        GameRegistry.addRecipe( new ShapedOreRecipe( ArchaicBlocks.blockTank, Array( "GGG", "GSG", "GGG", 'G', Blocks.glass, 'S',
            Items.iron_ingot ) ) )
        GameRegistry.addRecipe( new ShapedOreRecipe( ArchaicItems.itemHandCrank, Array( "S  ", "SSS", "  S", 'S', "stickWood" ) ) )
        GameRegistry.addRecipe( new ShapedOreRecipe( ArchaicItems.itemImprint, Array( "PPP", "PIP", "PPP", 'P', Items.paper, 'I',
            new ItemStack( Items.dye, 0 ) ) ) )
        GameRegistry.addRecipe( new ShapedOreRecipe( ArchaicItems.itemHammer, Array( "CC ", "CS ", "  S", 'C', "cobblestone", 'S',
            "stickWood" ) ) )
        proxy.postInit()
    }
}

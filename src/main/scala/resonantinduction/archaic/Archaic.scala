package resonantinduction.archaic

import resonantinduction.archaic.firebox.BlockFirebox
import archaic.firebox.BlockHotPlate
import resonantinduction.archaic.fluid.gutter.TileGutter
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary
import net.minecraftforge.oredict.ShapedOreRecipe
import resonant.content.loader.ModManager
import resonant.lib.content.ContentRegistry
import resonant.lib.modproxy.ProxyHandler
import resonant.lib.network.PacketAnnotation
import resonant.lib.network.PacketHandler
import resonant.lib.prefab.item.ItemBlockMetadata
import resonant.lib.recipe.UniversalRecipe
import resonantinduction.archaic.blocks.TileTurntable
import resonantinduction.archaic.crate.BlockCrate
import resonantinduction.archaic.crate.CrateCraftingHandler
import resonantinduction.archaic.crate.ItemBlockCrate
import resonantinduction.archaic.crate.TileCrate
import archaic.engineering.ItemHammer
import resonantinduction.archaic.engineering.TileEngineeringTable
import archaic.filter.BlockImprinter
import resonantinduction.archaic.filter.TileFilter
import archaic.filter.TileImprinter
import archaic.firebox.BlockHotPlate
import archaic.firebox.TileFirebox
import resonantinduction.archaic.firebox.TileHotPlate
import resonantinduction.archaic.fluid.grate.TileGrate
import resonantinduction.archaic.fluid.tank.TileTank
import archaic.process.BlockCastingMold
import archaic.process.BlockMillstone
import archaic.process.TileCastingMold
import archaic.process.TileMillstone
import resonantinduction.archaic.waila.Waila
import resonantinduction.core.Reference
import resonantinduction.core.ResonantInduction
import resonantinduction.core.ResonantTab
import resonantinduction.core.Settings
import resonantinduction.archaic.filter.imprint.ItemImprint
import resonantinduction.mechanical.gear.ItemHandCrank
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.Mod.Instance
import cpw.mods.fml.common.ModMetadata
import cpw.mods.fml.common.SidedProxy
import cpw.mods.fml.common.event.FMLInitializationEvent
import cpw.mods.fml.common.event.FMLPostInitializationEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.registry.GameRegistry
import Archaic._
//remove if not needed
import scala.collection.JavaConversions._

object Archaic {

    final val ID = "ResonantInduction|Archaic"

    final val NAME = Reference.NAME + " Archaic"

    val contentRegistry = new ModManager().setPrefix(Reference.PREFIX).setTab(ResonantTab.DEFAULT)

    @Instance(ID)
    var INSTANCE: Archaic = _

    @SidedProxy(clientSide = "resonantinduction.archaic.ClientProxy", serverSide = "resonantinduction.archaic.CommonProxy")
    var proxy: CommonProxy = _

    @Mod.Metadata(ID)
    var metadata: ModMetadata = _

}

@Mod(modid = Archaic.ID, name = Archaic.NAME, version = Reference.VERSION, dependencies = "required-after:" + ResonantInduction.ID)
class Archaic {

    var modproxies: ProxyHandler = _

    @EventHandler
    def preInit(evt: FMLPreInitializationEvent) {
        modproxies = new ProxyHandler()
        NetworkRegistry.instance().registerGuiHandler(this, proxy)
        Settings.config.load()
        ArchaicBlocks.blockEngineeringTable = contentRegistry.newBlock(classOf[TileEngineeringTable])
        ArchaicBlocks.blockCrate = contentRegistry.createBlock(classOf[BlockCrate], classOf[ItemBlockCrate], classOf[TileCrate])
        ArchaicBlocks.blockImprinter = contentRegistry.createTile(classOf[BlockImprinter], classOf[TileImprinter])
        ArchaicBlocks.blockTurntable = contentRegistry.newBlock(classOf[TileTurntable])
        ArchaicBlocks.blockFirebox = contentRegistry.createBlock(classOf[BlockFirebox], classOf[ItemBlockMetadata], classOf[TileFirebox])
        ArchaicBlocks.blockHotPlate = contentRegistry.createTile(classOf[BlockHotPlate], classOf[TileHotPlate])
        ArchaicBlocks.blockMillstone = contentRegistry.createTile(classOf[BlockMillstone], classOf[TileMillstone])
        ArchaicBlocks.blockCast = contentRegistry.createTile(classOf[BlockCastingMold], classOf[TileCastingMold])
        ArchaicBlocks.blockGutter = contentRegistry.newBlock(classOf[TileGutter])
        ArchaicBlocks.blockGrate = contentRegistry.newBlock(classOf[TileGrate])
        ArchaicBlocks.blockFilter = contentRegistry.newBlock(classOf[TileFilter])
        ArchaicBlocks.blockTank = contentRegistry.newBlock(classOf[TileTank])
        ArchaicItems.itemHandCrank = contentRegistry.createItem(classOf[ItemHandCrank])
        ArchaicItems.itemImprint = contentRegistry.createItem(classOf[ItemImprint])
        ArchaicItems.itemHammer = contentRegistry.createItem(classOf[ItemHammer])
        modproxies.applyModule(classOf[Waila], true)
        Settings.config.save()
        proxy.preInit()
        modproxies.preInit()
    }

    @EventHandler
    def init(evt: FMLInitializationEvent) {
        Settings.setModMetadata(metadata, ID, NAME, ResonantInduction.ID)
        proxy.init()
        modproxies.init()
    }

    @EventHandler
    def postInit(evt: FMLPostInitializationEvent) {
        ResonantTab.ITEMSTACK = new ItemStack(blockEngineeringTable)
        if (OreDictionary.getOres("cobblestone") == null) {
            OreDictionary.registerOre("cobblestone", Blocks.cobblestone)
        }
        if (OreDictionary.getOres("stickWood") == null) {
            OreDictionary.registerOre("stickWood", Items.stick)
        }
        GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockEngineeringTable, Array("P", "C", 'P', Blocks.wooden_pressure_plate, 'C', Blocks.crafting_table)))
        GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockFilter, Array("B", "P", "B", 'B', Blocks.iron_bars, 'P', Items.paper)))
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ArchaicBlocks.blockCrate, 1, 0), Array("WWW", "WSW", "WWW",
            'S', "stickWood", 'W', "logWood")))
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ArchaicBlocks.blockCrate, 1, 1), Array("WWW", "WSW", "WWW",
            'S', new ItemStack(ArchaicBlocks.blockCrate, 1, 0), 'W', "ingotIron")))
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ArchaicBlocks.blockCrate, 1, 2), Array("WWW", "WSW", "WWW",
            'S', new ItemStack(ArchaicBlocks.blockCrate, 1, 1), 'W', UniversalRecipe.PRIMARY_METAL.get)))
        GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockFirebox, Array("III", "SFS", "SSS", 'I', Items.iron_ingot,
            'F', Blocks.furnace, 'S', Blocks.stone)))
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ArchaicBlocks.blockFirebox, 1, 1), Array("III", "SFS", "SSS",
            'I', UniversalRecipe.PRIMARY_METAL.get, 'F', new ItemStack(ArchaicBlocks.blockFirebox, 1, 0), 'S', UniversalRecipe.WIRE.get)))
        GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockImprinter, Array("SSS", "W W", "PPP", 'S', Blocks.stone,
            'P', Blocks.piston, 'W', "logWood")))
        GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockTurntable, Array("SSS", "PGP", "WWW", 'S', Blocks.stone,
            'G', Items.redstone, 'P', Blocks.piston, 'W', "logWood")))
        GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockCast, Array("I I", "IBI", "III", 'S', Items.iron_ingot, 'B',
            Blocks.iron_bars)))
        GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockGutter, Array("S S", "I I", "III", 'S', Items.stick, 'I',
            "cobblestone")))
        GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockGrate, Array("WBW", "B B", "WBW", 'B', Blocks.iron_bars,
            'W', "plankWood")))
        GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockHotPlate, Array("SSS", "III", 'I', Items.iron_ingot, 'S',
            Blocks.stone)))
        GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockMillstone, Array("SPS", "SAS", "SSS", 'P', Blocks.piston,
            'A', Items.stone_pickaxe, 'S', Blocks.stone)))
        GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockTank, Array("GGG", "GSG", "GGG", 'G', Blocks.glass, 'S',
            Items.iron_ingot)))
        GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicItems.itemHandCrank, Array("S  ", "SSS", "  S", 'S', "stickWood")))
        GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicItems.itemImprint, Array("PPP", "PIP", "PPP", 'P', Items.paper, 'I',
            new ItemStack(Items.dye, 0))))
        GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicItems.itemHammer, Array("CC ", "CS ", "  S", 'C', "cobblestone", 'S',
            "stickWood")))
        GameRegistry.registerCraftingHandler(new CrateCraftingHandler())
        proxy.postInit()
        modproxies.postInit()
    }
}

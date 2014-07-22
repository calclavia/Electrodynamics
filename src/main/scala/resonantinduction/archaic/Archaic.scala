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
import cpw.mods.fml.common.network.NetworkMod
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
      OreDictionary.registerOre("cobblestone", Block.cobblestone)
    }
    if (OreDictionary.getOres("stickWood") == null) {
      OreDictionary.registerOre("stickWood", Item.stick)
    }
    GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockEngineeringTable, "P", "C", 'P', Blocks.pressurePlatePlanks,
      'C', Blocks.workbench))
    GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockFilter, "B", "P", "B", 'B', Blocks.fenceIron, 'P',
      Items.paper))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ArchaicBlocks.blockCrate, 1, 0), "WWW", "WSW", "WWW",
      'S', "stickWood", 'W', "logWood"))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ArchaicBlocks.blockCrate, 1, 1), "WWW", "WSW", "WWW",
      'S', new ItemStack(blockCrate, 1, 0), 'W', "ingotIron"))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ArchaicBlocks.blockCrate, 1, 2), "WWW", "WSW", "WWW",
      'S', new ItemStack(blockCrate, 1, 1), 'W', UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockFirebox, "III", "SFS", "SSS", 'I', Items.iron_ingot,
      'F', Block.furnaceIdle, 'S', Block.stone))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ArchaicBlocks.blockFirebox, 1, 1), "III", "SFS", "SSS",
      'I', UniversalRecipe.PRIMARY_METAL.get, 'F', new ItemStack(ArchaicBlocks.blockFirebox, 1, 0), 'S', UniversalRecipe.WIRE.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockImprinter, "SSS", "W W", "PPP", 'S', Blocks.stone,
      'P', Block.pistonBase, 'W', "logWood"))
    GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockTurntable, "SSS", "PGP", "WWW", 'S', Blocks.stone,
      'G', Item.redstone, 'P', Block.pistonBase, 'W', "logWood"))
    GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockCast, "I I", "IBI", "III", 'S', Items.ingotIron, 'B',
      Block.fenceIron))
    GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockGutter, "S S", "I I", "III", 'S', Items.stick, 'I',
      "cobblestone"))
    GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockGrate, "WBW", "B B", "WBW", 'B', Blocks.fenceIron,
      'W', "plankWood"))
    GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockHotPlate, "SSS", "III", 'I', Items.iron_ingot, 'S',
      Block.stone))
    GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockMillstone, "SPS", "SAS", "SSS", 'P', Blocks.pistonBase,
      'A', Item.pickaxeStone, 'S', Block.stone))
    GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicBlocks.blockTank, "GGG", "GSG", "GGG", 'G', Blocks.glass, 'S',
      Item.ingotIron))
    GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicItems.itemHandCrank, "S  ", "SSS", "  S", 'S', "stickWood"))
    GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicItems.itemImprint, "PPP", "PIP", "PPP", 'P', Items.paper, 'I',
      new ItemStack(Item.dyePowder, 0)))
    GameRegistry.addRecipe(new ShapedOreRecipe(ArchaicItems.itemHammer, "CC ", "CS ", "  S", 'C', "cobblestone", 'S',
      "stickWood"))
    GameRegistry.registerCraftingHandler(new CrateCraftingHandler())
    proxy.postInit()
    modproxies.postInit()
  }
}

package resonantinduction.archaic

import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.{Mod, SidedProxy}
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary
import resonant.content.loader.ModManager
import resonantinduction.archaic.blocks.{ItemImprint, TileImprinter, TileTurntable}
import resonantinduction.archaic.engineering.{ItemHammer, TileEngineeringTable}
import resonantinduction.archaic.firebox.{TileFirebox, TileHotPlate}
import resonantinduction.archaic.fluid.grate.TileGrate
import resonantinduction.archaic.fluid.gutter.TileGutter
import resonantinduction.archaic.fluid.tank.TileTank
import resonantinduction.archaic.process.{TileCastingMold, TileMillstone}
import resonantinduction.core.{Reference, ResonantTab, Settings}
import resonantinduction.mechanical.gear.ItemHandCrank

@Mod(modid = Archaic.ID, name = Archaic.NAME, version = Reference.version, modLanguage = "scala", dependencies = "required-after:" + Reference.coreID)
object Archaic
{

  final val ID = "ResonantInduction|Archaic"

  final val NAME = Reference.name + " Archaic"

  val contentRegistry = new ModManager().setPrefix(Reference.prefix).setTab(ResonantTab)

  var INSTANCE = this

  @SidedProxy(clientSide = "resonantinduction.archaic.ClientProxy", serverSide = "resonantinduction.archaic.CommonProxy")
  var proxy: CommonProxy = _

  @EventHandler
  def preInit(evt: FMLPreInitializationEvent)
  {
    NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy)
    Settings.config.load()
    ArchaicBlocks.blockEngineeringTable = contentRegistry.newBlock(classOf[TileEngineeringTable])
    //ArchaicBlocks.blockCrate = contentRegistry.newBlock( classOf[ BlockCrate ], classOf[ ItemBlockCrate ], classOf[ TileCrate ] )
    ArchaicBlocks.blockImprinter = contentRegistry.newBlock(classOf[TileImprinter])
    ArchaicBlocks.blockTurntable = contentRegistry.newBlock(classOf[TileTurntable])
    ArchaicBlocks.blockFirebox = contentRegistry.newBlock(classOf[TileFirebox])
    ArchaicBlocks.blockHotPlate = contentRegistry.newBlock(classOf[TileHotPlate])
    ArchaicBlocks.blockMillstone = contentRegistry.newBlock(classOf[TileMillstone])
    ArchaicBlocks.blockCast = contentRegistry.newBlock(classOf[TileCastingMold])
    ArchaicBlocks.blockGutter = contentRegistry.newBlock(classOf[TileGutter])
    ArchaicBlocks.blockGrate = contentRegistry.newBlock(classOf[TileGrate])
    //ArchaicBlocks.blockFilter = contentRegistry.newBlock( classOf[ TileFilter ] )
    ArchaicBlocks.blockTank = contentRegistry.newBlock(classOf[TileTank])
    ArchaicItems.itemHandCrank = contentRegistry.newItem(classOf[ItemHandCrank])
    ArchaicItems.itemImprint = contentRegistry.newItem(classOf[ItemImprint])
    ArchaicItems.itemHammer = contentRegistry.newItem(classOf[ItemHammer])
    Settings.config.save()
    proxy.preInit()
  }

  @EventHandler
  def init(evt: FMLInitializationEvent)
  {
    //Settings.setModMetadata( metadata, ID, NAME, ResonantInduction.ID )
    proxy.init()
  }

  @EventHandler
  def postInit(evt: FMLPostInitializationEvent)
  {
    ResonantTab.itemStack = new ItemStack(ArchaicBlocks.blockEngineeringTable)
    if (OreDictionary.getOres("cobblestone") == null)
    {
      OreDictionary.registerOre("cobblestone", Blocks.cobblestone)
    }
    if (OreDictionary.getOres("stickWood") == null)
    {
      OreDictionary.registerOre("stickWood", Items.stick)
    }

    ArchaicBlocks.postInit()
    ArchaicItems.postInit()
    proxy.postInit()
  }
}

package mffs

import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.common.{Mod, ModMetadata, SidedProxy}
import ic2.api.tile.ExplosionWhitelist
import mffs.base.{ItemMFFS, ItemModule}
import mffs.field.TileElectromagnetProjector
import mffs.field.mode._
import mffs.field.module._
import mffs.fortron.FortronHelper
import mffs.item.ItemRemoteController
import mffs.item.card._
import mffs.mobilize.TileForceMobilizer
import mffs.production._
import mffs.security.module._
import mffs.security.{TileBiometricIdentifier, TileInterdictionMatrix}
import net.minecraft.block.Block
import net.minecraft.init.{Items, Blocks}
import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fluids.{Fluid, FluidRegistry, FluidStack}
import net.minecraftforge.oredict.{OreDictionary, ShapedOreRecipe, ShapelessOreRecipe}
import org.modstats.{ModstatInfo, Modstats}
import resonant.api.mffs.Blacklist
import resonant.content.ModManager
import resonant.lib.config.ConfigHandler
import resonant.lib.network.netty.PacketManager
import resonant.lib.prefab.damage.CustomDamageSource
import resonant.lib.recipe.{RecipeUtility, UniversalRecipe}
import scala.collection.convert.wrapAll._

@Mod(modid = Reference.ID, name = Reference.NAME, version = Reference.VERSION, dependencies = "required-after:ResonantEngine")
@ModstatInfo(prefix = "mffs")
final object ModularForceFieldSystem
{
  //@Instance(Reference.ID)
  // var instance: ModularForceFieldSystem = _
  @Mod.Metadata(Reference.ID)
  var metadata: ModMetadata = _

  @SidedProxy(clientSide = "mffs.ClientProxy", serverSide = "mffs.CommonProxy")
  var proxy: CommonProxy = _

  val manager = new ModManager(Settings.configuration, Reference.ID).setTab(MFFSCreativeTab).setPrefix(Reference.PREFIX)

  /**
   * Machines
   */
  var blockCoercionDeriver: Block = _
  var blockFortronCapacitor: Block = _
  var blockForceFieldProjector: Block = _
  var blockBiometricIdentifier: Block = _
  var blockInterdictionMatrix: Block = _
  var blockForceManipulator: Block = _
  var blockForceField: Block = _
  /**
   * Items
   */
  var itemRemoteController: Item = _
  var itemFocusMatix: Item = _

  /**
   * Cards
   */
  var itemCardBlank: ItemCard = _
  var itemCardInfinite: ItemCard = _
  var itemCardFrequency: ItemCard = _
  var itemCardID: ItemCard = _
  var itemCardLink: ItemCard = _

  /**
   * Modes
   */
  var itemModeCube: ItemMode = _
  var itemModeSphere: ItemMode = _
  var itemModeTube: ItemMode = _
  var itemModeCylinder: ItemMode = _
  var itemModePyramid: ItemMode = _
  var itemModeCustom: ItemMode = _

  /**
   * Modules
   */
  var itemModule: ItemModule = _
  var itemModuleSpeed: ItemModule = _
  var itemModuleCapacity: ItemModule = _
  var itemModuleTranslate: ItemModule = _
  var itemModuleScale: ItemModule = _
  var itemModuleRotate: ItemModule = _
  var itemModuleCollection: ItemModule = _
  var itemModuleInvert: ItemModule = _
  var itemModuleSilence: ItemModule = _
  var itemModuleFusion: ItemModule = _
  var itemModuleDome: ItemModule = _
  var itemModuleCamouflage: ItemModule = _
  var itemModuleApproximation: ItemModule = _
  var itemModuleArray: ItemModule = _
  var itemModuleDisintegration: ItemModule = _
  var itemModuleShock: ItemModule = _
  var itemModuleGlow: ItemModule = _
  var itemModuleSponge: ItemModule = _
  var itemModuleStablize: ItemModule = _
  var itemModuleRepulsion: ItemModule = _
  var itemModuleAntiHostile: ItemModule = _
  var itemModuleAntiFriendly: ItemModule = _
  var itemModuleAntiPersonnel: ItemModule = _
  var itemModuleConfiscate: ItemModule = _
  var itemModuleWarn: ItemModule = _
  var itemModuleBlockAccess: ItemModule = _
  var itemModuleBlockAlter: ItemModule = _
  var itemModuleAntiSpawn: ItemModule = _

  /**
   * Damages
   */
  val damageFieldShock = new CustomDamageSource("fieldShock").setDamageBypassesArmor
  val packetHandler = new PacketManager()

  @EventHandler
  def preInit(event: FMLPreInitializationEvent)
  {
    Settings.load()
    Settings.configuration.load()

    /**
     * Block Instantiation
     */
    blockForceField = manager.newBlock(classOf[field.TileForceField])
    blockCoercionDeriver = manager.newBlock(classOf[TileCoercionDeriver])
    blockFortronCapacitor = manager.newBlock(classOf[TileFortronCapacitor])
    blockForceFieldProjector = manager.newBlock(classOf[TileElectromagnetProjector])
    blockBiometricIdentifier = manager.newBlock(classOf[TileBiometricIdentifier])
    blockInterdictionMatrix = manager.newBlock(classOf[TileInterdictionMatrix])
    blockForceManipulator = manager.newBlock(classOf[TileForceMobilizer])

    /**
     * Item Instantiation
     */
    itemRemoteController = manager.newItem(classOf[ItemRemoteController])
    itemFocusMatix = manager.newItem(classOf[ItemMFFS], "focusMatrix")
    itemModeCube = manager.newItem(classOf[ItemModeCube])
    itemModeSphere = manager.newItem(classOf[ItemModeSphere])
    itemModeTube = manager.newItem(classOf[ItemModeTube])
    itemModePyramid = manager.newItem(classOf[ItemModePyramid])
    itemModeCylinder = manager.newItem(classOf[ItemModeCylinder])
    itemModeCustom = manager.newItem(classOf[ItemModeCustom])
    itemModuleTranslate = manager.newItem(classOf[ItemModule], "moduleTranslate").setCost(2.5f)
    itemModuleScale = manager.newItem(classOf[ItemModule], "moduleScale").setCost(2.5f)
    itemModuleRotate = manager.newItem(classOf[ItemModule], "moduleRotate").setCost(0.5f)
    itemModuleSpeed = manager.newItem(classOf[ItemModule], "moduleSpeed").setCost(1f)
    itemModuleCapacity = manager.newItem(classOf[ItemModule], "moduleCapacity").setCost(0.5f)
    itemModuleFusion = manager.newItem(classOf[ItemModuleFusion])
    itemModuleDome = manager.newItem(classOf[ItemModuleDome])
    itemModuleCamouflage = manager.newItem(classOf[ItemModule], "moduleCamouflage").setCost(1.5f).setMaxStackSize(1)
    itemModuleDisintegration = manager.newItem(classOf[ItemModuleDisintegration])
    itemModuleShock = manager.newItem(classOf[ItemModuleShock])
    itemModuleGlow = manager.newItem(classOf[ItemModule], "moduleGlow")
    itemModuleSponge = manager.newItem(classOf[ItemModuleSponge])
    itemModuleStablize = manager.newItem(classOf[ItemModuleStablize])
    itemCardBlank = manager.newItem(classOf[ItemCard], "cardBlank")
    itemCardFrequency = manager.newItem(classOf[ItemCardFrequency])
    itemCardLink = manager.newItem(classOf[ItemCardLink])
    itemCardID = manager.newItem(classOf[ItemCardID])
    itemCardInfinite = manager.newItem(classOf[ItemCardInfinite])
    itemModuleAntiFriendly = manager.newItem(classOf[ItemModuleAntiFriendly])
    itemModuleAntiHostile = manager.newItem(classOf[ItemModuleAntiHostile])
    itemModuleAntiPersonnel = manager.newItem(classOf[ItemModuleAntiPersonnel])
    itemModuleConfiscate = manager.newItem(classOf[ItemModuleConfiscate])
    itemModuleWarn = manager.newItem(classOf[ItemModuleWarn])
    itemModuleBlockAccess = manager.newItem(classOf[ItemModuleInterdictionMatrix], "moduleBlockAccess").setCost(10)
    itemModuleBlockAlter = manager.newItem(classOf[ItemModuleInterdictionMatrix], "moduleBlockAlter").setCost(15)
    itemModuleAntiSpawn = manager.newItem(classOf[ItemModuleInterdictionMatrix], "moduleAntiSpawn").setCost(10)
    itemModuleCollection = manager.newItem(classOf[ItemModule], "moduleCollection").setMaxStackSize(1).setCost(15)
    itemModuleInvert = manager.newItem(classOf[ItemModule], "moduleInvert").setMaxStackSize(1).setCost(15)
    itemModuleSilence = manager.newItem(classOf[ItemModule], "moduleSilence").setMaxStackSize(1).setCost(1)
    itemModuleRepulsion = manager.newItem(classOf[ItemModuleRepulsion])
    itemModuleApproximation = manager.newItem(classOf[ItemModule], "moduleApproximation").setMaxStackSize(1).setCost(1f)
    itemModuleArray = manager.newItem(classOf[ItemModuleArray]).setCost(3f)

    /**
     * Registration
     */
    Modstats.instance.getReporter.registerMod(this)
    NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy)
    MinecraftForge.EVENT_BUS.register(SubscribeEventHandler)
    MinecraftForge.EVENT_BUS.register(itemRemoteController)

    /**
     * Fluid Instantiation
     */
    FortronHelper.FLUID_FORTRON = new Fluid("fortron")
    FortronHelper.FLUID_FORTRON.setGaseous(true)
    FluidRegistry.registerFluid(FortronHelper.FLUID_FORTRON)
    FortronHelper.FLUIDSTACK_FORTRON = new FluidStack(FortronHelper.FLUID_FORTRON, 0)


    Settings.configuration.save()

    proxy.preInit()
  }

  @EventHandler
  def load(evt: FMLInitializationEvent)
  {
    Blacklist.stabilizationBlacklist.add(Blocks.water)
    Blacklist.stabilizationBlacklist.add(Blocks.flowing_water)
    Blacklist.stabilizationBlacklist.add(Blocks.lava)
    Blacklist.stabilizationBlacklist.add(Blocks.flowing_lava)
    Blacklist.disintegrationBlacklist.add(Blocks.water)
    Blacklist.disintegrationBlacklist.add(Blocks.flowing_water)
    Blacklist.disintegrationBlacklist.add(Blocks.lava)
    Blacklist.stabilizationBlacklist.add(Blocks.flowing_lava)
    Blacklist.forceManipulationBlacklist.add(Blocks.bedrock)
    Blacklist.forceManipulationBlacklist.add(ModularForceFieldSystem.blockForceField)
    ExplosionWhitelist.addWhitelistedBlock(blockForceField)

    metadata.modId = Reference.ID
    metadata.name = Reference.NAME
    metadata.description = "Modular Force Field System is a mod that adds force fields, high tech machinery and defensive systems to Minecraft."
    metadata.url = "http://www.calclavia.com/mffs/"
    metadata.logoFile = "/mffs_logo.png"
    metadata.version = Reference.VERSION + "." + Reference.BUILD_VERSION
    metadata.authorList = Array[String]("Calclavia").toList
    metadata.credits = "Please visit the website."

    proxy.init()
  }

  @EventHandler
  def postInit(evt: FMLPostInitializationEvent)
  {
    Settings.configuration.load()

    /**
     * Add recipe.
     */
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemFocusMatix, 8), "RMR", "MDM", "RMR", 'M': Character, UniversalRecipe.PRIMARY_METAL.get, 'D': Character, Items.diamond, 'R': Character, Items.redstone))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemRemoteController), "WWW", "MCM", "MCM", 'W': Character, UniversalRecipe.WIRE.get, 'C': Character, UniversalRecipe.BATTERY.get, 'M': Character, UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCoercionDeriver), "FMF", "FCF", "FMF", 'C': Character, UniversalRecipe.BATTERY.get, 'M': Character, UniversalRecipe.PRIMARY_METAL.get, 'F': Character, itemFocusMatix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockFortronCapacitor), "MFM", "FCF", "MFM", 'D': Character, Items.diamond, 'C': Character, UniversalRecipe.BATTERY.get, 'F': Character, itemFocusMatix, 'M': Character, UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockForceFieldProjector), " D ", "FFF", "MCM", 'D': Character, Items.diamond, 'C': Character, UniversalRecipe.BATTERY.get, 'F': Character, itemFocusMatix, 'M': Character, UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockBiometricIdentifier), "FMF", "MCM", "FMF", 'C': Character, itemCardBlank, 'M': Character, UniversalRecipe.PRIMARY_METAL.get, 'F': Character, itemFocusMatix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockInterdictionMatrix), "SSS", "FFF", "FEF", 'S': Character, itemModuleShock, 'E': Character, Blocks.enderChest, 'F': Character, itemFocusMatix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockForceManipulator), "FCF", "TMT", "FCF", 'F': Character, itemFocusMatix, 'C': Character, UniversalRecipe.MOTOR.get, 'T': Character, itemModuleTranslate, 'M': Character, UniversalRecipe.MOTOR.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemCardBlank), "PPP", "PMP", "PPP", 'P': Character, Items.paper, 'M': Character, UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemCardLink), "BWB", 'B': Character, itemCardBlank, 'W': Character, UniversalRecipe.WIRE.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemCardFrequency), "WBW", 'B': Character, itemCardBlank, 'W': Character, UniversalRecipe.WIRE.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemCardID), "R R", " B ", "R R", 'B': Character, itemCardBlank, 'R': Character, Items.redstone))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModeSphere), " F ", "FFF", " F ", 'F': Character, itemFocusMatix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModeCube), "FFF", "FFF", "FFF", 'F': Character, itemFocusMatix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModeTube), "FFF", "   ", "FFF", 'F': Character, itemFocusMatix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModePyramid), "F  ", "FF ", "FFF", 'F': Character, itemFocusMatix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModeCylinder), "S", "S", "S", 'S': Character, itemModeSphere))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModeCustom), " C ", "TFP", " S ", 'S': Character, itemModeSphere, 'C': Character, itemModeCube, 'T': Character, itemModeTube, 'P': Character, itemModePyramid, 'F': Character, itemFocusMatix))
    GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(itemModeCustom), new ItemStack(itemModeCustom)))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleSpeed, 1), "FFF", "RRR", "FFF", 'F': Character, itemFocusMatix, 'R': Character, Items.redstone))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleCapacity, 2), "FCF", 'F': Character, itemFocusMatix, 'C': Character, UniversalRecipe.BATTERY.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleShock), "FWF", 'F': Character, itemFocusMatix, 'W': Character, UniversalRecipe.WIRE.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleSponge), "BBB", "BFB", "BBB", 'F': Character, itemFocusMatix, 'B': Character, Items.bucketWater))
    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleDisintegration), " W ", "FBF", " W ", 'F': Character, itemFocusMatix, 'W': Character, UniversalRecipe.WIRE.get, 'B': Character, UniversalRecipe.BATTERY.get), Settings.configuration, true)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleDome), "F", " ", "F", 'F': Character, itemFocusMatix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleCamouflage), "WFW", "FWF", "WFW", 'F': Character, itemFocusMatix, 'W': Character, new ItemStack(Blocks.cloth, 1, OreDictionary.WILDCARD_VALUE)))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleFusion), "FJF", 'F': Character, itemFocusMatix, 'J': Character, itemModuleShock))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleScale, 2), "FRF", 'F': Character, itemFocusMatix))
    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleTranslate, 2), "FSF", 'F': Character, itemFocusMatix, 'S': Character, itemModuleScale), Settings.configuration, true)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleRotate, 4), "F  ", " F ", "  F", 'F': Character, itemFocusMatix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleGlow, 4), "GGG", "GFG", "GGG", 'F': Character, itemFocusMatix, 'G': Character, Blocks.glowStone))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleStablize), "FDF", "PSA", "FDF", 'F': Character, itemFocusMatix, 'P': Character, Items.pickaxeDiamond, 'S': Character, Items.shovelDiamond, 'A': Character, Items.axeDiamond, 'D': Character, Items.diamond))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleCollection), "F F", " H ", "F F", 'F': Character, itemFocusMatix, 'H': Character, Blocks.hopperBlock))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleInvert), "L", "F", "L", 'F': Character, itemFocusMatix, 'L': Character, Blocks.blockLapis))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleSilence), " N ", "NFN", " N ", 'F': Character, itemFocusMatix, 'N': Character, Blocks.music))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleApproximation), " N ", "NFN", " N ", 'F': Character, itemFocusMatix, 'N': Character, Items.axeGold))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleArray), " F ", "DFD", " F ", 'F': Character, itemFocusMatix, 'D': Character, Items.diamond))
    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleRepulsion), "FFF", "DFD", "SFS", 'F': Character, itemFocusMatix, 'D': Character, Items.diamond, 'S': Character, Items.slimeBall), Settings.configuration, true)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleAntiHostile), " R ", "GFB", " S ", 'F': Character, itemFocusMatix, 'G': Character, Items.gunpowder, 'R': Character, Items.rottenFlesh, 'B': Character, Items.bone, 'S': Character, Items.ghastTear))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleAntiFriendly), " R ", "GFB", " S ", 'F': Character, itemFocusMatix, 'G': Character, Items.porkCooked, 'R': Character, new ItemStack(Blocks.cloth, 1, OreDictionary.WILDCARD_VALUE), 'B': Character, Items.leather, 'S': Character, Items.slimeBall))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleAntiPersonnel), "BFG", 'F': Character, itemFocusMatix, 'B': Character, itemModuleAntiHostile, 'G': Character, itemModuleAntiFriendly))
    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleConfiscate), "PEP", "EFE", "PEP", 'F': Character, itemFocusMatix, 'E': Character, Items.eyeOfEnder, 'P': Character, Items.enderPearl), Settings.configuration, true)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleWarn), "NFN", 'F': Character, itemFocusMatix, 'N': Character, Blocks.music))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleBlockAccess), " C ", "BFB", " C ", 'F': Character, itemFocusMatix, 'B': Character, Blocks.blockIron, 'C': Character, Blocks.chest))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleBlockAlter), " G ", "GFG", " G ", 'F': Character, itemModuleBlockAccess, 'G': Character, Blocks.blockGold))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleAntiSpawn), " H ", "G G", " H ", 'H': Character, itemModuleAntiHostile, 'G': Character, itemModuleAntiFriendly))

    proxy.postInit()

    try
    {
      ConfigHandler.configure(Settings.configuration, "mffs")
    }
    catch
      {
        case e: Exception =>
        {
          e.printStackTrace
        }
      }
    Settings.configuration.save()
  }

}
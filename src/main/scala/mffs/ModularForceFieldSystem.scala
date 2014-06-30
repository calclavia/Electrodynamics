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

  object Blocks
  {
    /**
     * Machines
     */
    var coercionDeriver: Block = _
    var fortronCapacitor: Block = _
    var forceFieldProjector: Block = _
    var biometricIdentifier: Block = _
    var interdictionMatrix: Block = _
    var forceManipulator: Block = _
    var forceField: Block = _
  }

  object Items
  {
    var remoteController: Item = _
    var focusMatix: Item = _

    /**
     * Cards
     */
    var cardBlank: ItemCard = _
    var cardInfinite: ItemCard = _
    var cardFrequency: ItemCard = _
    var cardID: ItemCard = _
    var cardLink: ItemCard = _

    /**
     * Modes
     */
    var modeCube: ItemMode = _
    var modeSphere: ItemMode = _
    var modeTube: ItemMode = _
    var modeCylinder: ItemMode = _
    var modePyramid: ItemMode = _
    var modeCustom: ItemMode = _

    /**
     * Modules
     */
    var module: ItemModule = _
    var moduleSpeed: ItemModule = _
    var moduleCapacity: ItemModule = _
    var moduleTranslate: ItemModule = _
    var moduleScale: ItemModule = _
    var moduleRotate: ItemModule = _
    var moduleCollection: ItemModule = _
    var moduleInvert: ItemModule = _
    var moduleSilence: ItemModule = _
    var moduleFusion: ItemModule = _
    var moduleDome: ItemModule = _
    var moduleCamouflage: ItemModule = _
    var moduleApproximation: ItemModule = _
    var moduleArray: ItemModule = _
    var moduleDisintegration: ItemModule = _
    var moduleShock: ItemModule = _
    var moduleGlow: ItemModule = _
    var moduleSponge: ItemModule = _
    var moduleStabilize: ItemModule = _
    var moduleRepulsion: ItemModule = _
    var moduleAntiHostile: ItemModule = _
    var moduleAntiFriendly: ItemModule = _
    var moduleAntiPersonnel: ItemModule = _
    var moduleConfiscate: ItemModule = _
    var moduleWarn: ItemModule = _
    var moduleBlockAccess: ItemModule = _
    var moduleBlockAlter: ItemModule = _
    var moduleAntiSpawn: ItemModule = _
  }

  /**
   * Damages
   */
  val damageFieldShock = new CustomDamageSource("fieldShock").setDamageBypassesArmor
  val packetHandler = new PacketManager()

  @EventHandler
  def preInit(event: FMLPreInitializationEvent)
  {
    Settings.configuration.load()

    /**
     * Block Instantiation
     */
    Blocks.forceField = manager.newBlock(classOf[field.TileForceField])
    Blocks.coercionDeriver = manager.newBlock(classOf[TileCoercionDeriver])
    Blocks.fortronCapacitor = manager.newBlock(classOf[TileFortronCapacitor])
    Blocks.forceFieldProjector = manager.newBlock(classOf[TileElectromagnetProjector])
    Blocks.biometricIdentifier = manager.newBlock(classOf[TileBiometricIdentifier])
    Blocks.interdictionMatrix = manager.newBlock(classOf[TileInterdictionMatrix])
    Blocks.forceManipulator = manager.newBlock(classOf[TileForceMobilizer])

    /**
     * Item Instantiation
     */
    Items.remoteController = manager.newItem(classOf[ItemRemoteController])
    Items.FocusMatix = manager.newItem(classOf[ItemMFFS], "focusMatrix")
    Items.ModeCube = manager.newItem(classOf[ItemModeCube])
    Items.ModeSphere = manager.newItem(classOf[ItemModeSphere])
    Items.ModeTube = manager.newItem(classOf[ItemModeTube])
    Items.ModePyramid = manager.newItem(classOf[ItemModePyramid])
    Items.ModeCylinder = manager.newItem(classOf[ItemModeCylinder])
    Items.ModeCustom = manager.newItem(classOf[ItemModeCustom])
    Items.ModuleTranslate = manager.newItem(classOf[ItemModule], "moduleTranslate").setCost(2.5f)
    Items.ModuleScale = manager.newItem(classOf[ItemModule], "moduleScale").setCost(2.5f)
    Items.ModuleRotate = manager.newItem(classOf[ItemModule], "moduleRotate").setCost(0.5f)
    Items.ModuleSpeed = manager.newItem(classOf[ItemModule], "moduleSpeed").setCost(1f)
    Items.ModuleCapacity = manager.newItem(classOf[ItemModule], "moduleCapacity").setCost(0.5f)
    Items.ModuleFusion = manager.newItem(classOf[ItemModuleFusion])
    Items.ModuleDome = manager.newItem(classOf[ItemModuleDome])
    Items.ModuleCamouflage = manager.newItem(classOf[ItemModule], "moduleCamouflage").setCost(1.5f).setMaxStackSize(1)
    Items.ModuleDisintegration = manager.newItem(classOf[ItemModuleDisintegration])
    Items.ModuleShock = manager.newItem(classOf[ItemModuleShock])
    Items.ModuleGlow = manager.newItem(classOf[ItemModule], "moduleGlow")
    Items.ModuleSponge = manager.newItem(classOf[ItemModuleSponge])
    Items.ModuleStablize = manager.newItem(classOf[ItemModuleStablize])
    Items.CardBlank = manager.newItem(classOf[ItemCard], "cardBlank")
    Items.CardFrequency = manager.newItem(classOf[ItemCardFrequency])
    Items.CardLink = manager.newItem(classOf[ItemCardLink])
    Items.CardID = manager.newItem(classOf[ItemCardID])
    Items.CardInfinite = manager.newItem(classOf[ItemCardInfinite])
    Items.ModuleAntiFriendly = manager.newItem(classOf[ItemModuleAntiFriendly])
    Items.ModuleAntiHostile = manager.newItem(classOf[ItemModuleAntiHostile])
    Items.ModuleAntiPersonnel = manager.newItem(classOf[ItemModuleAntiPersonnel])
    Items.ModuleConfiscate = manager.newItem(classOf[ItemModuleConfiscate])
    Items.ModuleWarn = manager.newItem(classOf[ItemModuleWarn])
    Items.ModuleBlockAccess = manager.newItem(classOf[ItemModuleInterdictionMatrix], "moduleBlockAccess").setCost(10)
    Items.ModuleBlockAlter = manager.newItem(classOf[ItemModuleInterdictionMatrix], "moduleBlockAlter").setCost(15)
    Items.ModuleAntiSpawn = manager.newItem(classOf[ItemModuleInterdictionMatrix], "moduleAntiSpawn").setCost(10)
    Items.ModuleCollection = manager.newItem(classOf[ItemModule], "moduleCollection").setMaxStackSize(1).setCost(15)
    Items.ModuleInvert = manager.newItem(classOf[ItemModule], "moduleInvert").setMaxStackSize(1).setCost(15)
    Items.ModuleSilence = manager.newItem(classOf[ItemModule], "moduleSilence").setMaxStackSize(1).setCost(1)
    Items.ModuleRepulsion = manager.newItem(classOf[ItemModuleRepulsion])
    Items.ModuleApproximation = manager.newItem(classOf[ItemModule], "moduleApproximation").setMaxStackSize(1).setCost(1f)
    Items.ModuleArray = manager.newItem(classOf[ItemModuleArray]).setCost(3f)

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
    Blacklist.stabilizationBlacklist.addAll(Settings.stabilizationBlacklist.map(Block.blockRegistry.getObject(_).asInstanceOf[Block]).toList)

    Blacklist.stabilizationBlacklist.add(Blocks.water)
    Blacklist.stabilizationBlacklist.add(Blocks.flowing_water)
    Blacklist.stabilizationBlacklist.add(Blocks.lava)
    Blacklist.stabilizationBlacklist.add(Blocks.flowing_lava)

    Blacklist.disintegrationBlacklist.addAll(Settings.disintegrationBlacklist.map(Block.blockRegistry.getObject(_).asInstanceOf[Block]).toList)

    Blacklist.disintegrationBlacklist.add(Blocks.water)
    Blacklist.disintegrationBlacklist.add(Blocks.flowing_water)
    Blacklist.disintegrationBlacklist.add(Blocks.lava)
    Blacklist.disintegrationBlacklist.add(Blocks.flowing_lava)

    Blacklist.mobilizerBlacklist.addAll(Settings.mobilizerBlacklist.map(Block.blockRegistry.getObject(_).asInstanceOf[Block]).toList)

    Blacklist.mobilizerBlacklist.add(Blocks.bedrock)
    Blacklist.mobilizerBlacklist.add(ModularForceFieldSystem.blockForceField)
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
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockInterdictionMatrix), "SSS", "FFF", "FEF", 'S': Character, itemModuleShock, 'E': Character, Blocks.ender_chest, 'F': Character, itemFocusMatix))
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
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleSponge), "BBB", "BFB", "BBB", 'F': Character, itemFocusMatix, 'B': Character, Items.water_bucket))
    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleDisintegration), " W ", "FBF", " W ", 'F': Character, itemFocusMatix, 'W': Character, UniversalRecipe.WIRE.get, 'B': Character, UniversalRecipe.BATTERY.get), Settings.configuration, true)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleDome), "F", " ", "F", 'F': Character, itemFocusMatix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleCamouflage), "WFW", "FWF", "WFW", 'F': Character, itemFocusMatix, 'W': Character, new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE)))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleFusion), "FJF", 'F': Character, itemFocusMatix, 'J': Character, itemModuleShock))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleScale, 2), "FRF", 'F': Character, itemFocusMatix))
    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleTranslate, 2), "FSF", 'F': Character, itemFocusMatix, 'S': Character, itemModuleScale), Settings.configuration, true)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleRotate, 4), "F  ", " F ", "  F", 'F': Character, itemFocusMatix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleGlow, 4), "GGG", "GFG", "GGG", 'F': Character, itemFocusMatix, 'G': Character, Blocks.glowstone))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleStablize), "FDF", "PSA", "FDF", 'F': Character, itemFocusMatix, 'P': Character, Items.diamond_pickaxe, 'S': Character, Items.diamond_shovel, 'A': Character, Items.diamond_axe, 'D': Character, Items.diamond))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleCollection), "F F", " H ", "F F", 'F': Character, itemFocusMatix, 'H': Character, Blocks.hopper))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleInvert), "L", "F", "L", 'F': Character, itemFocusMatix, 'L': Character, Blocks.lapis_block))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleSilence), " N ", "NFN", " N ", 'F': Character, itemFocusMatix, 'N': Character, Blocks.noteblock))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleApproximation), " N ", "NFN", " N ", 'F': Character, itemFocusMatix, 'N': Character, Items.golden_pickaxe))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleArray), " F ", "DFD", " F ", 'F': Character, itemFocusMatix, 'D': Character, Items.diamond))
    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleRepulsion), "FFF", "DFD", "SFS", 'F': Character, itemFocusMatix, 'D': Character, Items.diamond, 'S': Character, Items.slime_ball), Settings.configuration, true)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleAntiHostile), " R ", "GFB", " S ", 'F': Character, itemFocusMatix, 'G': Character, Items.gunpowder, 'R': Character, Items.rotten_flesh, 'B': Character, Items.bone, 'S': Character, Items.ghast_tear))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleAntiFriendly), " R ", "GFB", " S ", 'F': Character, itemFocusMatix, 'G': Character, Items.cooked_porkchop, 'R': Character, new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE), 'B': Character, Items.leather, 'S': Character, Items.slime_ball))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleAntiPersonnel), "BFG", 'F': Character, itemFocusMatix, 'B': Character, itemModuleAntiHostile, 'G': Character, itemModuleAntiFriendly))
    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleConfiscate), "PEP", "EFE", "PEP", 'F': Character, itemFocusMatix, 'E': Character, Items.ender_eye, 'P': Character, Items.ender_pearl), Settings.configuration, true)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleWarn), "NFN", 'F': Character, itemFocusMatix, 'N': Character, Blocks.noteblock))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleBlockAccess), " C ", "BFB", " C ", 'F': Character, itemFocusMatix, 'B': Character, Blocks.iron_block, 'C': Character, Blocks.chest))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleBlockAlter), " G ", "GFG", " G ", 'F': Character, itemModuleBlockAccess, 'G': Character, Blocks.gold_block))
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
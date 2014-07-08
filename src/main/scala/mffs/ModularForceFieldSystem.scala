package mffs

import java.util.UUID

import com.mojang.authlib.GameProfile
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
import mffs.item.ItemRemoteController
import mffs.item.card._
import mffs.mobilize.TileForceMobilizer
import mffs.production._
import mffs.security.TileBiometricIdentifier
import mffs.security.module._
import mffs.util.FortronUtility
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
   // var interdictionMatrix: Block = _
    var forceManipulator: Block = _
    var forceField: Block = _
  }

  object Items
  {
    var remoteController: Item = _
    var focusMatrix: Item = _

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

  import mffs.ModularForceFieldSystem.Blocks._
  import mffs.ModularForceFieldSystem.Items._
  import net.minecraft.init.Blocks._
  import net.minecraft.init.Items._

  /**
   * Damages
   */
  val damageFieldShock = new CustomDamageSource("fieldShock").setDamageBypassesArmor
  val fakeProfile = new GameProfile(UUID.randomUUID, "mffs")

  val packetHandler = new PacketManager()

  @EventHandler
  def preInit(event: FMLPreInitializationEvent)
  {
    Settings.configuration.load()

    /**
     * Block Instantiation
     */
    forceField = manager.newBlock(classOf[field.TileForceField])
    coercionDeriver = manager.newBlock(classOf[TileCoercionDeriver])
    fortronCapacitor = manager.newBlock(classOf[TileFortronCapacitor])
    forceFieldProjector = manager.newBlock(classOf[TileElectromagnetProjector])
    biometricIdentifier = manager.newBlock(classOf[TileBiometricIdentifier])
   // interdictionMatrix = manager.newBlock(classOf[TileInterdictionMatrix])
    forceManipulator = manager.newBlock(classOf[TileForceMobilizer])

    /**
     * Item Instantiation
     */
    remoteController = manager.newItem(classOf[ItemRemoteController])
    focusMatrix = manager.newItem(classOf[ItemMFFS], "focusMatrix")
    modeCube = manager.newItem(classOf[ItemModeCube])
    modeSphere = manager.newItem(classOf[ItemModeSphere])
    modeTube = manager.newItem(classOf[ItemModeTube])
    modePyramid = manager.newItem(classOf[ItemModePyramid])
    modeCylinder = manager.newItem(classOf[ItemModeCylinder])
    modeCustom = manager.newItem(classOf[ItemModeCustom])
    moduleTranslate = manager.newItem(classOf[ItemModule], "moduleTranslate").setCost(2.5f)
    moduleScale = manager.newItem(classOf[ItemModule], "moduleScale").setCost(2.5f)
    moduleRotate = manager.newItem(classOf[ItemModule], "moduleRotate").setCost(0.5f)
    moduleSpeed = manager.newItem(classOf[ItemModule], "moduleSpeed").setCost(1f)
    moduleCapacity = manager.newItem(classOf[ItemModule], "moduleCapacity").setCost(0.5f)
    moduleFusion = manager.newItem(classOf[ItemModuleFusion])
    moduleDome = manager.newItem(classOf[ItemModuleDome])
    moduleCamouflage = manager.newItem(classOf[ItemModule], "moduleCamouflage").setCost(1.5f).setMaxStackSize(1)
    moduleDisintegration = manager.newItem(classOf[ItemModuleDisintegration])
    moduleShock = manager.newItem(classOf[ItemModuleShock])
    moduleGlow = manager.newItem(classOf[ItemModule], "moduleGlow")
    moduleSponge = manager.newItem(classOf[ItemModuleSponge])
    moduleStabilize = manager.newItem(classOf[ItemModuleStabilize])
    cardBlank = manager.newItem(classOf[ItemCard], "cardBlank")
    cardFrequency = manager.newItem(classOf[ItemCardFrequency])
    cardLink = manager.newItem(classOf[ItemCardLink])
    cardID = manager.newItem(classOf[ItemCardIdentification])
    cardInfinite = manager.newItem(classOf[ItemCardInfinite])
    moduleAntiFriendly = manager.newItem(classOf[ItemModuleAntiFriendly])
    moduleAntiHostile = manager.newItem(classOf[ItemModuleAntiHostile])
    moduleAntiPersonnel = manager.newItem(classOf[ItemModuleAntiPersonnel])
    moduleConfiscate = manager.newItem(classOf[ItemModuleConfiscate])
    moduleWarn = manager.newItem(classOf[ItemModuleWarn])
    moduleBlockAccess = manager.newItem(classOf[ItemModuleDefense], "moduleBlockAccess").setCost(10)
    moduleBlockAlter = manager.newItem(classOf[ItemModuleDefense], "moduleBlockAlter").setCost(15)
    moduleAntiSpawn = manager.newItem(classOf[ItemModuleDefense], "moduleAntiSpawn").setCost(10)
    moduleCollection = manager.newItem(classOf[ItemModule], "moduleCollection").setMaxStackSize(1).setCost(15)
    moduleInvert = manager.newItem(classOf[ItemModule], "moduleInvert").setMaxStackSize(1).setCost(15)
    moduleSilence = manager.newItem(classOf[ItemModule], "moduleSilence").setMaxStackSize(1).setCost(1)
    moduleRepulsion = manager.newItem(classOf[ItemModuleRepulsion])
    moduleApproximation = manager.newItem(classOf[ItemModule], "moduleApproximation").setMaxStackSize(1).setCost(1f)
    moduleArray = manager.newItem(classOf[ItemModuleArray]).setCost(3f)

    /**
     * Registration
     */
    Modstats.instance.getReporter.registerMod(this)
    NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy)
    MinecraftForge.EVENT_BUS.register(SubscribeEventHandler)
    MinecraftForge.EVENT_BUS.register(remoteController)

    /**
     * Fluid Instantiation
     */
    FortronUtility.FLUID_FORTRON = new Fluid("fortron")
    FortronUtility.FLUID_FORTRON.setGaseous(true)
    FluidRegistry.registerFluid(FortronUtility.FLUID_FORTRON)
    FortronUtility.FLUIDSTACK_FORTRON = new FluidStack(FortronUtility.FLUID_FORTRON, 0)


    Settings.configuration.save()

    proxy.preInit()
  }

  @EventHandler
  def load(evt: FMLInitializationEvent)
  {
    Blacklist.stabilizationBlacklist.addAll(Settings.stabilizationBlacklist.map(Block.blockRegistry.getObject(_).asInstanceOf[Block]).toList)

    Blacklist.stabilizationBlacklist.add(water)
    Blacklist.stabilizationBlacklist.add(flowing_water)
    Blacklist.stabilizationBlacklist.add(lava)
    Blacklist.stabilizationBlacklist.add(flowing_lava)

    Blacklist.disintegrationBlacklist.addAll(Settings.disintegrationBlacklist.map(Block.blockRegistry.getObject(_).asInstanceOf[Block]).toList)

    Blacklist.disintegrationBlacklist.add(water)
    Blacklist.disintegrationBlacklist.add(flowing_water)
    Blacklist.disintegrationBlacklist.add(lava)
    Blacklist.disintegrationBlacklist.add(flowing_lava)

    Blacklist.mobilizerBlacklist.addAll(Settings.mobilizerBlacklist.map(Block.blockRegistry.getObject(_).asInstanceOf[Block]).toList)

    Blacklist.mobilizerBlacklist.add(bedrock)
    Blacklist.mobilizerBlacklist.add(forceField)
    ExplosionWhitelist.addWhitelistedBlock(forceField)

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
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(focusMatrix, 8), "RMR", "MDM", "RMR", 'M': Character, UniversalRecipe.PRIMARY_METAL.get, 'D': Character, diamond, 'R': Character, redstone))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(remoteController), "WWW", "MCM", "MCM", 'W': Character, UniversalRecipe.WIRE.get, 'C': Character, UniversalRecipe.BATTERY.get, 'M': Character, UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(coercionDeriver), "FMF", "FCF", "FMF", 'C': Character, UniversalRecipe.BATTERY.get, 'M': Character, UniversalRecipe.PRIMARY_METAL.get, 'F': Character, focusMatrix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(fortronCapacitor), "MFM", "FCF", "MFM", 'D': Character, diamond, 'C': Character, UniversalRecipe.BATTERY.get, 'F': Character, focusMatrix, 'M': Character, UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(forceFieldProjector), " D ", "FFF", "MCM", 'D': Character, diamond, 'C': Character, UniversalRecipe.BATTERY.get, 'F': Character, focusMatrix, 'M': Character, UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(biometricIdentifier), "FMF", "MCM", "FMF", 'C': Character, cardBlank, 'M': Character, UniversalRecipe.PRIMARY_METAL.get, 'F': Character, focusMatrix))
   // GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(interdictionMatrix), "SSS", "FFF", "FEF", 'S': Character, moduleShock, 'E': Character, ender_chest, 'F': Character, focusMatrix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(forceManipulator), "FCF", "TMT", "FCF", 'F': Character, focusMatrix, 'C': Character, UniversalRecipe.MOTOR.get, 'T': Character, moduleTranslate, 'M': Character, UniversalRecipe.MOTOR.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardBlank), "PPP", "PMP", "PPP", 'P': Character, paper, 'M': Character, UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardLink), "BWB", 'B': Character, cardBlank, 'W': Character, UniversalRecipe.WIRE.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardFrequency), "WBW", 'B': Character, cardBlank, 'W': Character, UniversalRecipe.WIRE.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardID), "R R", " B ", "R R", 'B': Character, cardBlank, 'R': Character, redstone))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(modeSphere), " F ", "FFF", " F ", 'F': Character, focusMatrix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(modeCube), "FFF", "FFF", "FFF", 'F': Character, focusMatrix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(modeTube), "FFF", "   ", "FFF", 'F': Character, focusMatrix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(modePyramid), "F  ", "FF ", "FFF", 'F': Character, focusMatrix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(modeCylinder), "S", "S", "S", 'S': Character, modeSphere))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(modeCustom), " C ", "TFP", " S ", 'S': Character, modeSphere, 'C': Character, modeCube, 'T': Character, modeTube, 'P': Character, modePyramid, 'F': Character, focusMatrix))
    GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(modeCustom), new ItemStack(modeCustom)))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleSpeed, 1), "FFF", "RRR", "FFF", 'F': Character, focusMatrix, 'R': Character, redstone))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleCapacity, 2), "FCF", 'F': Character, focusMatrix, 'C': Character, UniversalRecipe.BATTERY.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleShock), "FWF", 'F': Character, focusMatrix, 'W': Character, UniversalRecipe.WIRE.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleSponge), "BBB", "BFB", "BBB", 'F': Character, focusMatrix, 'B': Character, water_bucket))
    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(moduleDisintegration), " W ", "FBF", " W ", 'F': Character, focusMatrix, 'W': Character, UniversalRecipe.WIRE.get, 'B': Character, UniversalRecipe.BATTERY.get), Settings.configuration, true)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleDome), "F", " ", "F", 'F': Character, focusMatrix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleCamouflage), "WFW", "FWF", "WFW", 'F': Character, focusMatrix, 'W': Character, new ItemStack(wool, 1, OreDictionary.WILDCARD_VALUE)))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleFusion), "FJF", 'F': Character, focusMatrix, 'J': Character, moduleShock))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleScale, 2), "FRF", 'F': Character, focusMatrix))
    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(moduleTranslate, 2), "FSF", 'F': Character, focusMatrix, 'S': Character, moduleScale), Settings.configuration, true)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleRotate, 4), "F  ", " F ", "  F", 'F': Character, focusMatrix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleGlow, 4), "GGG", "GFG", "GGG", 'F': Character, focusMatrix, 'G': Character, glowstone))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleStabilize), "FDF", "PSA", "FDF", 'F': Character, focusMatrix, 'P': Character, diamond_pickaxe, 'S': Character, diamond_shovel, 'A': Character, diamond_axe, 'D': Character, diamond))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleCollection), "F F", " H ", "F F", 'F': Character, focusMatrix, 'H': Character, hopper))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleInvert), "L", "F", "L", 'F': Character, focusMatrix, 'L': Character, lapis_block))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleSilence), " N ", "NFN", " N ", 'F': Character, focusMatrix, 'N': Character, noteblock))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleApproximation), " N ", "NFN", " N ", 'F': Character, focusMatrix, 'N': Character, golden_pickaxe))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleArray), " F ", "DFD", " F ", 'F': Character, focusMatrix, 'D': Character, diamond))
    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(moduleRepulsion), "FFF", "DFD", "SFS", 'F': Character, focusMatrix, 'D': Character, diamond, 'S': Character, slime_ball), Settings.configuration, true)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleAntiHostile), " R ", "GFB", " S ", 'F': Character, focusMatrix, 'G': Character, gunpowder, 'R': Character, rotten_flesh, 'B': Character, bone, 'S': Character, ghast_tear))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleAntiFriendly), " R ", "GFB", " S ", 'F': Character, focusMatrix, 'G': Character, cooked_porkchop, 'R': Character, new ItemStack(wool, 1, OreDictionary.WILDCARD_VALUE), 'B': Character, leather, 'S': Character, slime_ball))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleAntiPersonnel), "BFG", 'F': Character, focusMatrix, 'B': Character, moduleAntiHostile, 'G': Character, moduleAntiFriendly))
    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(moduleConfiscate), "PEP", "EFE", "PEP", 'F': Character, focusMatrix, 'E': Character, ender_eye, 'P': Character, ender_pearl), Settings.configuration, true)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleWarn), "NFN", 'F': Character, focusMatrix, 'N': Character, noteblock))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleBlockAccess), " C ", "BFB", " C ", 'F': Character, focusMatrix, 'B': Character, iron_block, 'C': Character, chest))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleBlockAlter), " G ", "GFG", " G ", 'F': Character, moduleBlockAccess, 'G': Character, gold_block))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleAntiSpawn), " H ", "G G", " H ", 'H': Character, moduleAntiHostile, 'G': Character, moduleAntiFriendly))

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
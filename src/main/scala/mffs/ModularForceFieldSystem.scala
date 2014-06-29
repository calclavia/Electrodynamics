package mffs

import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLServerStartingEvent, FMLPostInitializationEvent, FMLInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.common.{Mod, ModMetadata, SidedProxy}
import ic2.api.tile.ExplosionWhitelist
import mffs.base.ItemMFFS
import mffs.card.ItemCard
import mffs.fortron.FortronHelper
import mffs.item.ItemRemoteController
import mffs.item.card.{ItemCardFrequency, ItemCardID, ItemCardInfinite, ItemCardLink}
import mffs.item.mode._
import mffs.item.module.ItemModule
import mffs.item.module.interdiction._
import mffs.item.module.projector._
import mffs.tile._
import net.minecraft.block.Block
import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fluids.{Fluid, FluidStack, FluidRegistry}
import net.minecraftforge.oredict.{OreDictionary, ShapedOreRecipe, ShapelessOreRecipe}
import org.modstats.{ModstatInfo, Modstats}
import resonant.api.mffs.Blacklist
import resonant.content.ModManager
import resonant.lib.config.ConfigHandler
import resonant.lib.network.netty.PacketManager
import resonant.lib.prefab.damage.CustomDamageSource
import resonant.lib.recipe.{RecipeUtility, UniversalRecipe}

@Mod(modid = Reference.ID, name = Reference.NAME, version = Reference.VERSION, dependencies = "required-after:ResonantEngine")
@ModstatInfo(prefix = "mffs")
object ModularForceFieldSystem
{
  //@Instance(Reference.ID)
  // var instance: ModularForceFieldSystem = _
  @Mod.Metadata(Reference.ID)
  var metadata: ModMetadata = _

  @SidedProxy(clientSide = "mffs.ClientProxy", serverSide = "mffs.CommonProxy")
  var proxy: CommonProxy = _

  val manager = new ModManager(Settings.configuration, Reference.ID)

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
    Modstats.instance.getReporter.registerMod(this)
    NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy)
    MinecraftForge.EVENT_BUS.register(new SubscribeEventHandler)

    Settings.load()
    Settings.configuration.load()

    blockForceField = manager.newBlock(classOf[field.TileForceField])
    blockCoercionDeriver = manager.newBlock(classOf[TileCoercionDeriver])
    blockFortronCapacitor = manager.newBlock(classOf[TileFortronCapacitor])
    blockForceFieldProjector = manager.newBlock(classOf[TileForceFieldProjector])
    blockBiometricIdentifier = manager.newBlock(classOf[TileBiometricIdentifier])
    blockInterdictionMatrix = manager.newBlock(classOf[TileInterdictionMatrix])
    blockForceManipulator = manager.newBlock(classOf[TileForceManipulator])

    itemRemoteController = new ItemRemoteController
    itemFocusMatix = new ItemMFFS("focusMatrix")
    itemModeCube = new ItemModeCube
    itemModeSphere = new ItemModeSphere
    itemModeTube = new ItemModeTube
    itemModePyramid = new ItemModePyramid
    itemModeCylinder = new ItemModeCylinder
    itemModeCustom = new ItemModeCustom
    itemModuleTranslate = new ItemModule("moduleTranslate").setCost(2.5f)
    itemModuleScale = new ItemModule("moduleScale").setCost(2.5f)
    itemModuleRotate = new ItemModule("moduleRotate").setCost(0.5f)
    itemModuleSpeed = new ItemModule("moduleSpeed").setCost(1f)
    itemModuleCapacity = new ItemModule("moduleCapacity").setCost(0.5f)
    itemModuleFusion = new ItemModuleFusion
    itemModuleDome = new ItemModuleDome
    itemModuleCamouflage = new ItemModule("moduleCamouflage").setCost(1.5f).setMaxStackSize(1)
    itemModuleDisintegration = new ItemModuleDisintegration
    itemModuleShock = new ItemModuleShock
    itemModuleGlow = new ItemModule("moduleGlow")
    itemModuleSponge = new ItemModuleSponge
    itemModuleStablize = new ItemModuleStablize
    itemCardBlank = new ItemCard("cardBlank")
    itemCardFrequency = new ItemCardFrequency
    itemCardLink = new ItemCardLink
    itemCardID = new ItemCardID
    itemCardInfinite = new ItemCardInfinite
    itemModuleAntiFriendly = new ItemModuleAntiFriendly
    itemModuleAntiHostile = new ItemModuleAntiHostile
    itemModuleAntiPersonnel = new ItemModuleAntiPersonnel
    itemModuleConfiscate = new ItemModuleConfiscate
    itemModuleWarn = new ItemModuleWarn
    itemModuleBlockAccess = new ItemModuleInterdictionMatrix("moduleBlockAccess").setCost(10)
    itemModuleBlockAlter = new ItemModuleInterdictionMatrix("moduleBlockAlter").setCost(15)
    itemModuleAntiSpawn = new ItemModuleInterdictionMatrix("moduleAntiSpawn").setCost(10)
    itemModuleCollection = new ItemModule("moduleCollection").setMaxStackSize(1).setCost(15)
    itemModuleInvert = new ItemModule("moduleInvert").setMaxStackSize(1).setCost(15)
    itemModuleSilence = new ItemModule("moduleSilence").setMaxStackSize(1).setCost(1)

    MinecraftForge.EVENT_BUS.register(itemRemoteController)
    FortronHelper.FLUID_FORTRON = new Fluid("fortron")
    FortronHelper.FLUID_FORTRON.setGaseous(true)
    FluidRegistry.registerFluid(FortronHelper.FLUID_FORTRON)
    FortronHelper.FLUIDSTACK_FORTRON = new FluidStack(FortronHelper.FLUID_FORTRON, 0)
    itemModuleRepulsion = new ItemModuleRepulsion
    itemModuleApproximation = new ItemModule("moduleApproximation").setMaxStackSize(1).setCost(1f)
    itemModuleArray = new ItemModuleArray().setCost(3f)

    Settings.configuration.save()

    proxy.preInit()
  }

  @EventHandler
  def load(evt: FMLInitializationEvent)
  {
    Blacklist.stabilizationBlacklist.add(Block.waterStill.blockID)
    Blacklist.stabilizationBlacklist.add(Block.waterMoving.blockID)
    Blacklist.stabilizationBlacklist.add(Block.lavaStill.blockID)
    Blacklist.stabilizationBlacklist.add(Block.lavaMoving.blockID)
    Blacklist.disintegrationBlacklist.add(Block.waterStill.blockID)
    Blacklist.disintegrationBlacklist.add(Block.waterMoving.blockID)
    Blacklist.disintegrationBlacklist.add(Block.lavaStill.blockID)
    Blacklist.stabilizationBlacklist.add(Block.lavaMoving.blockID)
    Blacklist.forceManipulationBlacklist.add(Block.bedrock.blockID)
    Blacklist.forceManipulationBlacklist.add(ModularForceFieldSystem.blockForceField.blockID)
    ExplosionWhitelist.addWhitelistedBlock(blockForceField)

    metadata.modId = Reference.ID
    metadata.name = Reference.NAME
    metadata.description = "Modular Force Field System is a mod that adds force fields, high tech machinery and defensive systems to Minecraft."
    metadata.url = "http://www.calclavia.com/mffs/"
    metadata.logoFile = "/mffs_logo.png"
    metadata.version = VERSION + "." + BUILD_VERSION
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
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemFocusMatix, 8), "RMR", "MDM", "RMR", 'M', UniversalRecipe.PRIMARY_METAL.get, 'D', Item.diamond, 'R', Item.redstone))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemRemoteController), "WWW", "MCM", "MCM", 'W', UniversalRecipe.WIRE.get, 'C', UniversalRecipe.BATTERY.get, 'M', UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCoercionDeriver), "FMF", "FCF", "FMF", 'C', UniversalRecipe.BATTERY.get, 'M', UniversalRecipe.PRIMARY_METAL.get, 'F', itemFocusMatix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockFortronCapacitor), "MFM", "FCF", "MFM", 'D', Item.diamond, 'C', UniversalRecipe.BATTERY.get, 'F', itemFocusMatix, 'M', UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockForceFieldProjector), " D ", "FFF", "MCM", 'D', Item.diamond, 'C', UniversalRecipe.BATTERY.get, 'F', itemFocusMatix, 'M', UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockBiometricIdentifier), "FMF", "MCM", "FMF", 'C', itemCardBlank, 'M', UniversalRecipe.PRIMARY_METAL.get, 'F', itemFocusMatix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockInterdictionMatrix), "SSS", "FFF", "FEF", 'S', itemModuleShock, 'E', Block.enderChest, 'F', itemFocusMatix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockForceManipulator), "FCF", "TMT", "FCF", 'F', itemFocusMatix, 'C', UniversalRecipe.MOTOR.get, 'T', itemModuleTranslate, 'M', UniversalRecipe.MOTOR.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemCardBlank), "PPP", "PMP", "PPP", 'P', Item.paper, 'M', UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemCardLink), "BWB", 'B', itemCardBlank, 'W', UniversalRecipe.WIRE.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemCardFrequency), "WBW", 'B', itemCardBlank, 'W', UniversalRecipe.WIRE.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemCardID), "R R", " B ", "R R", 'B', itemCardBlank, 'R', Item.redstone))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModeSphere), " F ", "FFF", " F ", 'F', itemFocusMatix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModeCube), "FFF", "FFF", "FFF", 'F', itemFocusMatix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModeTube), "FFF", "   ", "FFF", 'F', itemFocusMatix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModePyramid), "F  ", "FF ", "FFF", 'F', itemFocusMatix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModeCylinder), "S", "S", "S", 'S', itemModeSphere))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModeCustom), " C ", "TFP", " S ", 'S', itemModeSphere, 'C', itemModeCube, 'T', itemModeTube, 'P', itemModePyramid, 'F', itemFocusMatix))
    GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(itemModeCustom), new ItemStack(itemModeCustom)))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleSpeed, 1), "FFF", "RRR", "FFF", 'F', itemFocusMatix, 'R', Item.redstone))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleCapacity, 2), "FCF", 'F', itemFocusMatix, 'C', UniversalRecipe.BATTERY.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleShock), "FWF", 'F', itemFocusMatix, 'W', UniversalRecipe.WIRE.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleSponge), "BBB", "BFB", "BBB", 'F', itemFocusMatix, 'B', Item.bucketWater))
    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleDisintegration), " W ", "FBF", " W ", 'F', itemFocusMatix, 'W', UniversalRecipe.WIRE.get, 'B', UniversalRecipe.BATTERY.get), Settings.configuration, true)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleDome), "F", " ", "F", 'F', itemFocusMatix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleCamouflage), "WFW", "FWF", "WFW", 'F', itemFocusMatix, 'W', new ItemStack(Block.cloth, 1, OreDictionary.WILDCARD_VALUE)))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleFusion), "FJF", 'F', itemFocusMatix, 'J', itemModuleShock))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleScale, 2), "FRF", 'F', itemFocusMatix))
    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleTranslate, 2), "FSF", 'F', itemFocusMatix, 'S', itemModuleScale), Settings.configuration, true)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleRotate, 4), "F  ", " F ", "  F", 'F', itemFocusMatix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleGlow, 4), "GGG", "GFG", "GGG", 'F', itemFocusMatix, 'G', Block.glowStone))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleStablize), "FDF", "PSA", "FDF", 'F', itemFocusMatix, 'P', Item.pickaxeDiamond, 'S', Item.shovelDiamond, 'A', Item.axeDiamond, 'D', Item.diamond))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleCollection), "F F", " H ", "F F", 'F', itemFocusMatix, 'H', Block.hopperBlock))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleInvert), "L", "F", "L", 'F', itemFocusMatix, 'L', Block.blockLapis))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleSilence), " N ", "NFN", " N ", 'F', itemFocusMatix, 'N', Block.music))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleApproximation), " N ", "NFN", " N ", 'F', itemFocusMatix, 'N', Item.axeGold))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleArray), " F ", "DFD", " F ", 'F', itemFocusMatix, 'D', Item.diamond))
    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleRepulsion), "FFF", "DFD", "SFS", 'F', itemFocusMatix, 'D', Item.diamond, 'S', Item.slimeBall), Settings.configuration, true)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleAntiHostile), " R ", "GFB", " S ", 'F', itemFocusMatix, 'G', Item.gunpowder, 'R', Item.rottenFlesh, 'B', Item.bone, 'S', Item.ghastTear))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleAntiFriendly), " R ", "GFB", " S ", 'F', itemFocusMatix, 'G', Item.porkCooked, 'R', new ItemStack(Block.cloth, 1, OreDictionary.WILDCARD_VALUE), 'B', Item.leather, 'S', Item.slimeBall))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleAntiPersonnel), "BFG", 'F', itemFocusMatix, 'B', itemModuleAntiHostile, 'G', itemModuleAntiFriendly))
    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleConfiscate), "PEP", "EFE", "PEP", 'F', itemFocusMatix, 'E', Item.eyeOfEnder, 'P', Item.enderPearl), Settings.configuration, true)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleWarn), "NFN", 'F', itemFocusMatix, 'N', Block.music))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleBlockAccess), " C ", "BFB", " C ", 'F', itemFocusMatix, 'B', Block.blockIron, 'C', Block.chest))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleBlockAlter), " G ", "GFG", " G ", 'F', itemModuleBlockAccess, 'G', Block.blockGold))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemModuleAntiSpawn), " H ", "G G", " H ", 'H', itemModuleAntiHostile, 'G', itemModuleAntiFriendly))

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
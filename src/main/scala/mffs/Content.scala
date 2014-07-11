package mffs

import cpw.mods.fml.common.registry.GameRegistry
import mffs.base.{ItemMFFS, ItemModule}
import mffs.field.mobilize.TileForceMobilizer
import mffs.field.mode._
import mffs.field.module._
import mffs.field.{TileElectromagneticProjector, TileForceField}
import mffs.item.ItemRemoteController
import mffs.item.card.{ItemCard, ItemCardFrequency, ItemCardIdentification, ItemCardLink}
import mffs.item.fortron.ItemCardInfinite
import mffs.production.{TileCoercionDeriver, TileFortronCapacitor}
import mffs.security.TileBiometricIdentifier
import mffs.security.module._
import net.minecraft.block.Block
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.{OreDictionary, ShapedOreRecipe, ShapelessOreRecipe}
import resonant.content.loader.{ContentHolder, ImplicitContentName}
import resonant.lib.recipe.{RecipeUtility, UniversalRecipe}

/**
 * The main content of MFFS
 * @author Calclavia
 */
object Content extends ContentHolder
{
  /**
   * Blocks
   */
  val coercionDeriver: Block = new TileCoercionDeriver
  val fortronCapacitor: Block = new TileFortronCapacitor
  val forceFieldProjector: Block = new TileElectromagneticProjector
  val biometricIdentifier: Block = new TileBiometricIdentifier
  val forceManipulator: Block = new TileForceMobilizer
  val forceField: Block = new TileForceField

  /**
   * Misc Items
   */
  val remoteController = new ItemRemoteController
  @ImplicitContentName
  val focusMatrix = new ItemMFFS()

  /**
   * Cards
   */
  @ImplicitContentName
  val cardBlank = new ItemCard
  val cardInfinite = new ItemCardInfinite
  val cardFrequency = new ItemCardFrequency
  val cardID = new ItemCardIdentification
  val cardLink = new ItemCardLink

  /**
   * Modes
   */
  val modeCube = new ItemModeCube
  val modeSphere = new ItemModeSphere
  val modeTube = new ItemModeTube
  val modeCylinder = new ItemModeCylinder
  val modePyramid = new ItemModePyramid
  val modeCustom = new ItemModeCustom

  /**
   * Modules
   */
  @ImplicitContentName
  val moduleTranslate = new ItemModule().setCost(3f)
  @ImplicitContentName
  val moduleScale = new ItemModule().setCost(2.5f)
  @ImplicitContentName
  val moduleRotate = new ItemModule().setCost(0.5f)
  @ImplicitContentName
  val moduleSpeed = new ItemModule().setCost(1.5f)
  @ImplicitContentName
  val moduleCapacity = new ItemModule().setCost(0.5f)
  @ImplicitContentName
  val moduleCollection = new ItemModule().setMaxStackSize(1).setCost(15)
  @ImplicitContentName
  val moduleInvert = new ItemModule().setMaxStackSize(1).setCost(15)
  @ImplicitContentName
  val moduleSilence = new ItemModule().setMaxStackSize(1).setCost(1)
  val moduleFusion = new ItemModuleFusion
  val moduleDome = new ItemModuleDome
  @ImplicitContentName
  val moduleCamouflage = new ItemModule().setCost(1.5f).setMaxStackSize(1)
  @ImplicitContentName
  val moduleApproximation = new ItemModule().setMaxStackSize(1).setCost(1f)
  val moduleArray = new ItemModuleArray().setCost(3f)
  val moduleDisintegration = new ItemModuleDisintegration
  val moduleShock = new ItemModuleShock
  @ImplicitContentName
  val moduleGlow = new ItemModule
  val moduleSponge = new ItemModuleSponge
  val moduleStabilize = new ItemModuleStabilize
  val moduleRepulsion = new ItemModuleRepulsion
  val moduleAntiHostile = new ItemModuleAntiHostile
  val moduleAntiFriendly = new ItemModuleAntiFriendly
  val moduleAntiPersonnel = new ItemModuleAntiPersonnel
  val moduleConfiscate = new ItemModuleConfiscate
  val moduleWarn = new ItemModuleWarn
  @ImplicitContentName
  val moduleBlockAccess = new ItemModuleDefense().setCost(10)
  @ImplicitContentName
  val moduleBlockAlter = new ItemModuleDefense().setCost(15)
  @ImplicitContentName
  val moduleAntiSpawn = new ItemModuleDefense().setCost(10)

  manager.setTab(MFFSCreativeTab).setPrefix(Reference.prefix)

  override def postInit()
  {
    /**
     * Add recipe.
     */
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(focusMatrix, 8), "RMR", "MDM", "RMR", 'M': Character, UniversalRecipe.PRIMARY_METAL.get, 'D': Character, Items.diamond, 'R': Character, Items.redstone))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(remoteController), "WWW", "MCM", "MCM", 'W': Character, UniversalRecipe.WIRE.get, 'C': Character, UniversalRecipe.BATTERY.get, 'M': Character, UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(coercionDeriver), "FMF", "FCF", "FMF", 'C': Character, UniversalRecipe.BATTERY.get, 'M': Character, UniversalRecipe.PRIMARY_METAL.get, 'F': Character, focusMatrix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(fortronCapacitor), "MFM", "FCF", "MFM", 'D': Character, Items.diamond, 'C': Character, UniversalRecipe.BATTERY.get, 'F': Character, focusMatrix, 'M': Character, UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(forceFieldProjector), " D ", "FFF", "MCM", 'D': Character, Items.diamond, 'C': Character, UniversalRecipe.BATTERY.get, 'F': Character, focusMatrix, 'M': Character, UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(biometricIdentifier), "FMF", "MCM", "FMF", 'C': Character, cardBlank, 'M': Character, UniversalRecipe.PRIMARY_METAL.get, 'F': Character, focusMatrix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(forceManipulator), "FCF", "TMT", "FCF", 'F': Character, focusMatrix, 'C': Character, UniversalRecipe.MOTOR.get, 'T': Character, moduleTranslate, 'M': Character, UniversalRecipe.MOTOR.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardBlank), "PPP", "PMP", "PPP", 'P': Character, Items.paper, 'M': Character, UniversalRecipe.PRIMARY_METAL.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardLink), "BWB", 'B': Character, cardBlank, 'W': Character, UniversalRecipe.WIRE.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardFrequency), "WBW", 'B': Character, cardBlank, 'W': Character, UniversalRecipe.WIRE.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardID), "R R", " B ", "R R", 'B': Character, cardBlank, 'R': Character, Items.redstone))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(modeSphere), " F ", "FFF", " F ", 'F': Character, focusMatrix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(modeCube), "FFF", "FFF", "FFF", 'F': Character, focusMatrix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(modeTube), "FFF", "   ", "FFF", 'F': Character, focusMatrix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(modePyramid), "F  ", "FF ", "FFF", 'F': Character, focusMatrix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(modeCylinder), "S", "S", "S", 'S': Character, modeSphere))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(modeCustom), " C ", "TFP", " S ", 'S': Character, modeSphere, 'C': Character, modeCube, 'T': Character, modeTube, 'P': Character, modePyramid, 'F': Character, focusMatrix))
    GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(modeCustom), new ItemStack(modeCustom)))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleSpeed, 1), "FFF", "RRR", "FFF", 'F': Character, focusMatrix, 'R': Character, Items.redstone))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleCapacity, 2), "FCF", 'F': Character, focusMatrix, 'C': Character, UniversalRecipe.BATTERY.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleShock), "FWF", 'F': Character, focusMatrix, 'W': Character, UniversalRecipe.WIRE.get))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleSponge), "BBB", "BFB", "BBB", 'F': Character, focusMatrix, 'B': Character, Items.water_bucket))
    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(moduleDisintegration), " W ", "FBF", " W ", 'F': Character, focusMatrix, 'W': Character, UniversalRecipe.WIRE.get, 'B': Character, UniversalRecipe.BATTERY.get), Settings.config, true)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleDome), "F", " ", "F", 'F': Character, focusMatrix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleCamouflage), "WFW", "FWF", "WFW", 'F': Character, focusMatrix, 'W': Character, new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE)))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleFusion), "FJF", 'F': Character, focusMatrix, 'J': Character, moduleShock))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleScale, 2), "FRF", 'F': Character, focusMatrix))
    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(moduleTranslate, 2), "FSF", 'F': Character, focusMatrix, 'S': Character, moduleScale), Settings.config, true)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleRotate, 4), "F  ", " F ", "  F", 'F': Character, focusMatrix))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleGlow, 4), "GGG", "GFG", "GGG", 'F': Character, focusMatrix, 'G': Character, Blocks.glowstone))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleStabilize), "FDF", "PSA", "FDF", 'F': Character, focusMatrix, 'P': Character, Items.diamond_pickaxe, 'S': Character, Items.diamond_shovel, 'A': Character, Items.diamond_axe, 'D': Character, Items.diamond))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleCollection), "F F", " H ", "F F", 'F': Character, focusMatrix, 'H': Character, Blocks.hopper))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleInvert), "L", "F", "L", 'F': Character, focusMatrix, 'L': Character, Blocks.lapis_block))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleSilence), " N ", "NFN", " N ", 'F': Character, focusMatrix, 'N': Character, Blocks.noteblock))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleApproximation), " N ", "NFN", " N ", 'F': Character, focusMatrix, 'N': Character, Items.golden_pickaxe))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleArray), " F ", "DFD", " F ", 'F': Character, focusMatrix, 'D': Character, Items.diamond))
    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(moduleRepulsion), "FFF", "DFD", "SFS", 'F': Character, focusMatrix, 'D': Character, Items.diamond, 'S': Character, Items.slime_ball), Settings.config, true)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleAntiHostile), " R ", "GFB", " S ", 'F': Character, focusMatrix, 'G': Character, Items.gunpowder, 'R': Character, Items.rotten_flesh, 'B': Character, Items.bone, 'S': Character, Items.ghast_tear))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleAntiFriendly), " R ", "GFB", " S ", 'F': Character, focusMatrix, 'G': Character, Items.cooked_porkchop, 'R': Character, new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE), 'B': Character, Items.leather, 'S': Character, Items.slime_ball))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleAntiPersonnel), "BFG", 'F': Character, focusMatrix, 'B': Character, moduleAntiHostile, 'G': Character, moduleAntiFriendly))
    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(moduleConfiscate), "PEP", "EFE", "PEP", 'F': Character, focusMatrix, 'E': Character, Items.ender_eye, 'P': Character, Items.ender_pearl), Settings.config, true)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleWarn), "NFN", 'F': Character, focusMatrix, 'N': Character, Blocks.noteblock))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleBlockAccess), " C ", "BFB", " C ", 'F': Character, focusMatrix, 'B': Character, Blocks.iron_block, 'C': Character, Blocks.chest))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleBlockAlter), " G ", "GFG", " G ", 'F': Character, moduleBlockAccess, 'G': Character, Blocks.gold_block))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(moduleAntiSpawn), " H ", "G G", " H ", 'H': Character, moduleAntiHostile, 'G': Character, moduleAntiFriendly))
  }
}

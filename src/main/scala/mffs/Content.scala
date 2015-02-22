package mffs

import com.resonant.core.prefab.modcontent.ContentLoader
import mffs.base.ItemModule
import mffs.field.mobilize.TileForceMobilizer
import mffs.field.mode._
import mffs.field.{TileElectromagneticProjector, TileForceField}
import mffs.item.ItemRemoteController
import mffs.item.card.{ItemCard, ItemCardFrequency, ItemCardLink}
import mffs.item.fortron.ItemCardInfinite
import mffs.production.{TileCoercionDeriver, TileFortronCapacitor}
import mffs.security.TileBiometricIdentifier
import mffs.security.card.ItemCardIdentification
import nova.core.block.Block
import nova.core.item.Item

/**
 * The main content of MFFS
 * @author Calclavia
 */
object Content extends ContentLoader {
	/**
	 * Blocks
	 */
	val coercionDeriver: Block = classOf[TileCoercionDeriver]
	val fortronCapacitor: Block = classOf[TileFortronCapacitor]
	val electromagneticProjector: Block = classOf[TileElectromagneticProjector]
	val biometricIdentifier: Block = classOf[TileBiometricIdentifier]
	val forceMobilizer: Block = classOf[TileForceMobilizer]
	val forceField: Block = classOf[TileForceField]

	/**
	 * Misc Items
	 */
	val remoteController = new ItemRemoteController
	val focusMatrix = new ItemMFFS()

	/**
	 * Cards
	 */
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
	@ExplicitContentName
	val moduleTranslate = new ItemModule().setCost(3f)
	@ExplicitContentName
	val moduleScale = new ItemModule().setCost(2.5f)
	@ExplicitContentName
	val moduleRotate = new ItemModule().setCost(0.5f)
	@ExplicitContentName
	val moduleSpeed = new ItemModule().setCost(1.5f)
	@ExplicitContentName
	val moduleCapacity = new ItemModule().setCost(0.5f)
	@ExplicitContentName
	val moduleCollection = new ItemModule().setMaxStackSize(1).setCost(15)
	@ExplicitContentName
	val moduleInvert = new ItemModule().setMaxStackSize(1).setCost(15)
	@ExplicitContentName
	val moduleSilence = new ItemModule().setMaxStackSize(1).setCost(1)
	val moduleFusion = new ItemModuleFusion
	val moduleDome = new ItemModuleDome
	@ExplicitContentName
	val moduleCamouflage = new ItemModule().setCost(1.5f).setMaxStackSize(1)
	@ExplicitContentName
	val moduleApproximation = new ItemModule().setMaxStackSize(1).setCost(1f)
	val moduleArray = new ItemModuleArray().setCost(3f)
	val moduleDisintegration = new ItemModuleDisintegration
	val moduleShock = new ItemModuleShock
	@ExplicitContentName
	val moduleGlow = new ItemModule
	val moduleSponge = new ItemModuleSponge
	val moduleStabilize = new ItemModuleStabilize
	val moduleRepulsion = new ItemModuleRepulsion
	val moduleAntiHostile = new ItemModuleAntiHostile().setCost(10)
	val moduleAntiFriendly = new ItemModuleAntiFriendly().setCost(5)
	val moduleAntiPersonnel = new ItemModuleAntiPersonnel().setCost(15)
	val moduleConfiscate = new ItemModuleConfiscate
	val moduleWarn = new ItemModuleBroadcast
	@ExplicitContentName
	val moduleBlockAccess = new ItemModuleDefense().setCost(10)
	@ExplicitContentName
	val moduleBlockAlter = new ItemModuleDefense().setCost(15)
	@ExplicitContentName
	val moduleAntiSpawn = new ItemModuleDefense().setCost(10)

	manager.setTab(MFFSCreativeTab).setPrefix(Reference.prefix)

	override def postInit() {
		/**
		 * Add recipe.
		 */
		recipes += shaped(new Item(focusMatrix, 8), "RMR", "MDM", "RMR", 'M', UniversalRecipe.PRIMARY_METAL.get, 'D', Items.diamond, 'R', Items.redstone)
		recipes += shaped(new Item(remoteController), "WWW", "MCM", "MCM", 'W', UniversalRecipe.WIRE.get, 'C', UniversalRecipe.BATTERY.get, 'M', UniversalRecipe.PRIMARY_METAL.get)
		recipes += shaped(new Item(coercionDeriver), "FMF", "FCF", "FMF", 'C', UniversalRecipe.BATTERY.get, 'M', UniversalRecipe.PRIMARY_METAL.get, 'F', focusMatrix)
		recipes += shaped(new Item(fortronCapacitor), "MFM", "FCF", "MFM", 'D', Items.diamond, 'C', UniversalRecipe.BATTERY.get, 'F', focusMatrix, 'M', UniversalRecipe.PRIMARY_METAL.get)
		recipes += shaped(new Item(electromagneticProjector), " D ", "FFF", "MCM", 'D', Items.diamond, 'C', UniversalRecipe.BATTERY.get, 'F', focusMatrix, 'M', UniversalRecipe.PRIMARY_METAL.get)
		recipes += shaped(new Item(biometricIdentifier), "FMF", "MCM", "FMF", 'C', cardBlank, 'M', UniversalRecipe.PRIMARY_METAL.get, 'F', focusMatrix)
		recipes += shaped(new Item(forceMobilizer), "FCF", "TMT", "FCF", 'F', focusMatrix, 'C', UniversalRecipe.MOTOR.get, 'T', moduleTranslate, 'M', UniversalRecipe.MOTOR.get)
		recipes += shaped(new Item(cardBlank), "PPP", "PMP", "PPP", 'P', Items.paper, 'M', UniversalRecipe.PRIMARY_METAL.get)
		recipes += shaped(new Item(cardLink), "BWB", 'B', cardBlank, 'W', UniversalRecipe.WIRE.get)
		recipes += shaped(new Item(cardFrequency), "WBW", 'B', cardBlank, 'W', UniversalRecipe.WIRE.get)
		recipes += shaped(new Item(cardID), "R R", " B ", "R R", 'B', cardBlank, 'R', Items.redstone)
		recipes += shaped(new Item(modeSphere), " F ", "FFF", " F ", 'F', focusMatrix)
		recipes += shaped(new Item(modeCube), "FFF", "FFF", "FFF", 'F', focusMatrix)
		recipes += shaped(new Item(modeTube), "FFF", "   ", "FFF", 'F', focusMatrix)
		recipes += shaped(new Item(modePyramid), "F  ", "FF ", "FFF", 'F', focusMatrix)
		recipes += shaped(new Item(modeCylinder), "S", "S", "S", 'S', modeSphere)
		recipes += shaped(new Item(modeCustom), " C ", "TFP", " S ", 'S', modeSphere, 'C', modeCube, 'T', modeTube, 'P', modePyramid, 'F', focusMatrix)
		recipes += shapeless(new Item(modeCustom), new Item(modeCustom))
		recipes += shaped(new Item(moduleSpeed, 1), "FFF", "RRR", "FFF", 'F', focusMatrix, 'R', Items.redstone)
		recipes += shaped(new Item(moduleCapacity, 2), "FCF", 'F', focusMatrix, 'C', UniversalRecipe.BATTERY.get)
		recipes += shaped(new Item(moduleShock), "FWF", 'F', focusMatrix, 'W', UniversalRecipe.WIRE.get)
		recipes += shaped(new Item(moduleSponge), "BBB", "BFB", "BBB", 'F', focusMatrix, 'B', Items.water_bucket)
		recipes += shaped(new Item(moduleDisintegration), " W ", "FBF", " W ", 'F', focusMatrix, 'W', UniversalRecipe.WIRE.get, 'B', UniversalRecipe.BATTERY.get).config(Settings.config)
		recipes += shaped(new Item(moduleDome), "F", " ", "F", 'F', focusMatrix)
		recipes += shaped(new Item(moduleCamouflage), "WFW", "FWF", "WFW", 'F', focusMatrix, 'W', new Item(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE))
		recipes += shaped(new Item(moduleFusion), "FJF", 'F', focusMatrix, 'J', moduleShock)
		recipes += shaped(new Item(moduleScale, 2), "FRF", 'F', focusMatrix)
		recipes += shaped(new Item(moduleTranslate, 2), "FSF", 'F', focusMatrix, 'S', moduleScale).config(Settings.config)
		recipes += shaped(new Item(moduleRotate, 4), "F  ", " F ", "  F", 'F', focusMatrix)
		recipes += shaped(new Item(moduleGlow, 4), "GGG", "GFG", "GGG", 'F', focusMatrix, 'G', Blocks.glowstone)
		recipes += shaped(new Item(moduleStabilize), "FDF", "PSA", "FDF", 'F', focusMatrix, 'P', Items.diamond_pickaxe, 'S', Items.diamond_shovel, 'A', Items.diamond_axe, 'D', Items.diamond)
		recipes += shaped(new Item(moduleCollection), "F F", " H ", "F F", 'F', focusMatrix, 'H', Blocks.hopper)
		recipes += shaped(new Item(moduleInvert), "L", "F", "L", 'F', focusMatrix, 'L', Blocks.lapis_block)
		recipes += shaped(new Item(moduleSilence), " N ", "NFN", " N ", 'F', focusMatrix, 'N', Blocks.noteblock)
		recipes += shaped(new Item(moduleApproximation), " N ", "NFN", " N ", 'F', focusMatrix, 'N', Items.golden_pickaxe)
		recipes += shaped(new Item(moduleArray), " F ", "DFD", " F ", 'F', focusMatrix, 'D', Items.diamond)
		recipes += shaped(new Item(moduleRepulsion), "FFF", "DFD", "SFS", 'F', focusMatrix, 'D', Items.diamond, 'S', Items.slime_ball).config(Settings.config)
		recipes += shaped(new Item(moduleAntiHostile), " R ", "GFB", " S ", 'F', focusMatrix, 'G', Items.gunpowder, 'R', Items.rotten_flesh, 'B', Items.bone, 'S', Items.ghast_tear)
		recipes += shaped(new Item(moduleAntiFriendly), " R ", "GFB", " S ", 'F', focusMatrix, 'G', Items.cooked_porkchop, 'R', new
				Item(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE), 'B', Items.leather, 'S', Items.slime_ball)
		recipes += shaped(new Item(moduleAntiPersonnel), "BFG", 'F', focusMatrix, 'B', moduleAntiHostile, 'G', moduleAntiFriendly)
		recipes += shaped(new Item(moduleConfiscate), "PEP", "EFE", "PEP", 'F', focusMatrix, 'E', Items.ender_eye, 'P', Items.ender_pearl).config(Settings.config)
		recipes += shaped(new Item(moduleWarn), "NFN", 'F', focusMatrix, 'N', Blocks.noteblock)
		recipes += shaped(new Item(moduleBlockAccess), " C ", "BFB", " C ", 'F', focusMatrix, 'B', Blocks.iron_block, 'C', Blocks.chest)
		recipes += shaped(new Item(moduleBlockAlter), " G ", "GFG", " G ", 'F', moduleBlockAccess, 'G', Blocks.gold_block)
		recipes += shaped(new Item(moduleAntiSpawn), " H ", "G G", " H ", 'H', moduleAntiHostile, 'G', moduleAntiFriendly)
	}
}

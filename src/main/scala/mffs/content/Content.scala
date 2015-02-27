package mffs.content

import com.resonant.core.prefab.modcontent.{ContentLoader, RecipeHolder}
import mffs.base.{ItemModule, Named}
import mffs.field.mobilize.BlockMobilizer
import mffs.field.module._
import mffs.field.shape._
import mffs.field.{BlockForceField, BlockProjector}
import mffs.item.ItemRemoteController
import mffs.item.card.{ItemCard, ItemCardFrequency, ItemCardInfinite, ItemCardLink}
import mffs.particle.{FXFortronBeam, FXHologram, FXHologramProgress, FieldColor}
import mffs.production.{BlockCoercionDeriver, BlockFortronCapacitor}
import mffs.security.BlockBiometric
import mffs.security.card.ItemCardIdentification
import mffs.security.module._
import nova.core.block.Block
import nova.core.entity.EntityFactory
import nova.core.item.Item

/**
 * The main mffs.content of MFFS
 * @author Calclavia
 */
object Content extends ContentLoader with RecipeHolder {
	/**
	 * Blocks
	 */
	val coercionDeriver: Block = classOf[BlockCoercionDeriver]
	val fortronCapacitor: Block = classOf[BlockFortronCapacitor]
	val electromagneticProjector: Block = classOf[BlockProjector]
	val biometricIdentifier: Block = classOf[BlockBiometric]
	val forceMobilizer: Block = classOf[BlockMobilizer]
	val forceField: Block = classOf[BlockForceField]

	/**
	 * Misc Items
	 */
	val remoteController: Item = classOf[ItemRemoteController]
	val focusMatrix: Item = () => (new Item with Named).setName("focusMatrix")

	/**
	 * Cards
	 */
	val cardBlank: Item = classOf[ItemCard]
	val cardInfinite: Item = classOf[ItemCardInfinite]
	val cardFrequency: Item = classOf[ItemCardFrequency]
	val cardID: Item = classOf[ItemCardIdentification]
	val cardLink: Item = classOf[ItemCardLink]

	/**
	 * Shapes
	 */
	val modeCube: Item = classOf[ItemShapeCube]
	val modeSphere: Item = classOf[ItemShapeSphere]
	val modeTube: Item = classOf[ItemShapeTube]
	val modeCylinder: Item = classOf[ItemShapeCylinder]
	val modePyramid: Item = classOf[ItemShapePyramid]
	val modeCustom: Item = classOf[ItemShapeCustom]

	/**
	 * Modules
	 */
	val moduleTranslate: Item = () => (new ItemModule with Named).setName("moduleTranslate").setCost(3f)
	val moduleScale: Item = () => (new ItemModule with Named).setName("moduleScale").setCost(2.5f)
	val moduleRotate: Item = () => (new ItemModule with Named).setName("moduleRotate").setCost(0.5f)
	val moduleSpeed: Item = () => (new ItemModule with Named).setName("moduleSpeed").setCost(1.5f)
	val moduleCapacity: Item = () => (new ItemModule with Named).setName("moduleCapacity").setCost(0.5f)
	val moduleCollection: Item = () => (new ItemModule with Named).setName("moduleCollection").setMaxCount(1).setCost(15)
	val moduleInvert: Item = () => (new ItemModule with Named).setName("moduleInvert").setMaxCount(1).setCost(15)
	val moduleSilence: Item = () => (new ItemModule with Named).setName("moduleSilence").setMaxCount(1).setCost(1)
	val moduleFusion: Item = new ItemModuleFusion
	val moduleDome: Item = classOf[ItemModuleDome]
	val moduleCamouflage: Item = () => (new ItemModule with Named).setName("moduleCamouflage").setCost(1.5f).setMaxCount(1)
	val moduleApproximation: Item = () => (new ItemModule with Named).setName("moduleApproximation").setMaxCount(1).setCost(1f)
	val moduleArray: Item = new ItemModuleArray().setCost(3f)
	val moduleDisintegration: Item = new ItemModuleDisintegration
	val moduleShock: Item = new ItemModuleShock
	val moduleGlow: Item = () => (new ItemModule with Named).setName("moduleGlow")
	val moduleSponge: Item = classOf[ItemModuleSponge]
	val moduleStabilize: Item = classOf[ItemModuleStabilize]
	val moduleRepulsion: Item = classOf[ItemModuleRepulsion]
	val moduleAntiHostile: Item = () => new ItemModuleAntiHostile().setCost(10)
	val moduleAntiFriendly: Item = () => new ItemModuleAntiFriendly().setCost(5)
	val moduleAntiPersonnel: Item = () => new ItemModuleAntiPersonnel().setCost(15)
	val moduleConfiscate: Item = classOf[ItemModuleConfiscate]
	val moduleWarn: Item = classOf[ItemModuleBroadcast]
	val moduleBlockAccess: Item = () => new ItemModuleDefense().setCost(10)
	val moduleBlockAlter: Item = () => new ItemModuleDefense().setCost(15)
	val moduleAntiSpawn: Item = () => new ItemModuleDefense().setCost(10)

	val fxFortronBeam: EntityFactory = () => new FXFortronBeam(FieldColor.blue, 40)
	val fxHologram: EntityFactory = () => new FXHologram(FieldColor.blue, 40)
	val fxHologramProgress: EntityFactory = () => new FXHologramProgress(FieldColor.blue, 40)

	override def postInit() {
		/**
		 * Add recipe.
		
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
		 */
	}
}

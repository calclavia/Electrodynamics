package com.calclavia.edx.optics.content

import com.calclavia.edx.core.Reference
import com.calclavia.edx.optics.beam.fx.EntityMagneticBeam
import com.calclavia.edx.optics.beam.{BlockCrystal, BlockLaserEmitter, BlockLaserReceiver, BlockMirror}
import com.calclavia.edx.optics.component.{ItemModule, Named}
import com.calclavia.edx.optics.field.module._
import com.calclavia.edx.optics.field.shape._
import com.calclavia.edx.optics.field.{BlockForceField, BlockProjector}
import com.calclavia.edx.optics.fx.{FXHologram, FXHologramProgress}
import com.calclavia.edx.optics.item.ItemRemoteController
import com.calclavia.edx.optics.item.card.{ItemCard, ItemCardFrequency, ItemCardInfinite, ItemCardLink}
import com.calclavia.edx.optics.security.BlockBiometric
import com.calclavia.edx.optics.security.card.ItemCardIdentification
import com.calclavia.edx.optics.security.module._
import nova.core.block.BlockFactory
import nova.core.entity.EntityFactory
import nova.core.item.{Item, ItemFactory}
import nova.core.render.Color
import nova.core.sound.{SoundManager, Sound, SoundFactory}
import nova.scala.modcontent.ContentLoader
import nova.scala.wrapper.FunctionalWrapper._

/**
 * The main mffs.content of MFFS
 * @author Calclavia
 */
object OpticsContent extends ContentLoader {

	/**
	 * Blocks
	 */
	val electromagneticProjector: BlockFactory = classOf[BlockProjector]
	val biometricIdentifier: BlockFactory = classOf[BlockBiometric]
	val forceField: BlockFactory = classOf[BlockForceField]

	val laserEmitter: BlockFactory = classOf[BlockLaserEmitter]
	val laserReceiver: BlockFactory = classOf[BlockLaserReceiver]
	val mirror: BlockFactory = classOf[BlockMirror]
	val crystal: BlockFactory = classOf[BlockCrystal]

	/**
	 * Misc Items
	 */
	val remoteController: ItemFactory = classOf[ItemRemoteController]
	val focusMatrix: ItemFactory = () => (new Item with Named).setName("focusMatrix")
	/**
	 * Cards
	 */
	val cardBlank: ItemFactory = classOf[ItemCard]
	val cardInfinite: ItemFactory = classOf[ItemCardInfinite]
	val cardFrequency: ItemFactory = classOf[ItemCardFrequency]
	val cardID: ItemFactory = classOf[ItemCardIdentification]
	val cardLink: ItemFactory = classOf[ItemCardLink]
	/**
	 * Shapes
	 */
	val modeCube: ItemFactory = classOf[ItemShapeCube]
	val modeSphere: ItemFactory = classOf[ItemShapeSphere]
	val modeTube: ItemFactory = classOf[ItemShapeTube]
	val modeCylinder: ItemFactory = classOf[ItemShapeCylinder]
	val modePyramid: ItemFactory = classOf[ItemShapePyramid]
	val modeCustom: ItemFactory = classOf[ItemShapeCustom]
	/**
	 * Modules
	 */
	val moduleTranslate: ItemFactory = () => (new ItemModule with Named).setName("moduleTranslate").setCost(3f)
	val moduleScale: ItemFactory = () => (new ItemModule with Named).setName("moduleScale").setCost(2.5f)
	val moduleRotate: ItemFactory = () => (new ItemModule with Named).setName("moduleRotate").setCost(0.5f)
	val moduleSpeed: ItemFactory = () => (new ItemModule with Named).setName("moduleSpeed").setCost(1.5f)
	val moduleCapacity: ItemFactory = () => (new ItemModule with Named).setName("moduleCapacity").setCost(0.5f)
	val moduleCollection: ItemFactory = () => (new ItemModule with Named).setName("moduleCollection").setMaxCount(1).setCost(15)
	val moduleInvert: ItemFactory = () => (new ItemModule with Named).setName("moduleInvert").setMaxCount(1).setCost(15)
	val moduleSilence: ItemFactory = () => (new ItemModule with Named).setName("moduleSilence").setMaxCount(1).setCost(1)
	val moduleFusion: ItemFactory = classOf[ItemModuleFusion]
	val moduleDome: ItemFactory = classOf[ItemModuleDome]
	val moduleCamouflage: ItemFactory = () => (new ItemModule with Named).setName("moduleCamouflage").setCost(1.5f).setMaxCount(1)
	val moduleApproximation: ItemFactory = () => (new ItemModule with Named).setName("moduleApproximation").setMaxCount(1).setCost(1f)
	val moduleArray: ItemFactory = () => new ItemModuleArray().setCost(3f)
	val moduleShock: ItemFactory = classOf[ItemModuleShock]
	val moduleGlow: ItemFactory = () => (new ItemModule with Named).setName("moduleGlow")
	val moduleSponge: ItemFactory = classOf[ItemModuleSponge]
	val moduleRepulsion: ItemFactory = classOf[ItemModuleRepulsion]
	val moduleAntiHostile: ItemFactory = () => new ItemModuleAntiHostile().setCost(10)
	val moduleAntiFriendly: ItemFactory = () => new ItemModuleAntiFriendly().setCost(5)
	val moduleAntiPersonnel: ItemFactory = () => new ItemModuleAntiPersonnel().setCost(15)
	val moduleConfiscate: ItemFactory = classOf[ItemModuleConfiscate]
	val moduleWarn: ItemFactory = classOf[ItemModuleBroadcast]
	val moduleBlockAccess: ItemFactory = () => (new ItemModuleDefense with Named).setCost(10).setName("moduleBlockAccess")
	val moduleBlockAlter: ItemFactory = () => (new ItemModuleDefense with Named).setCost(15).setName("moduleBlockAlter")
	val moduleAntiSpawn: ItemFactory = () => (new ItemModuleDefense with Named).setCost(10).setName("moduleAntiSpawn")
	//TODO: Allow args.
	val fxFortronBeam: EntityFactory = () => new EntityMagneticBeam(Color.blue, 40)
	val fxHologram: EntityFactory = () => new FXHologram(Color.blue, 40)
	val fxHologramProgress: EntityFactory = () => new FXHologramProgress(Color.blue, 40)

	//Sound
	val fieldSoundfactory = new SoundFactory(func(args => new Sound("edx", "field")))

	override def id: String = Reference.id

	override def preInit() {
		super.preInit()

	}

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

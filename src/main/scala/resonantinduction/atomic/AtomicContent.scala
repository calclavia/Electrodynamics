package resonantinduction.atomic

import net.minecraft.block.Block
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.fluids.{Fluid, FluidStack}
import resonant.content.loader.ContentHolder
import resonant.lib.ore.OreGenerator
import resonant.lib.recipe.UniversalRecipe
import resonantinduction.core.{Reference, ResonantTab}

/**
 * Created by robert on 8/10/2014.
 */
object AtomicContent extends ContentHolder
{


  manager.setTab(ResonantTab).setPrefix(Reference.prefix)

  /** Block and Items */
  var blockRadioactive: Block = null
  var blockCentrifuge: Block = null
  var blockNuclearBoiler: Block = null
  var blockControlRod: Block = null
  var blockThermometer: Block = null
  var blockFusionCore: Block = null
  var blockPlasma: Block = null
  var blockElectromagnet: Block = null
  var blockChemicalExtractor: Block = null
  var blockSiren: Block = null
  var blockSteamFunnel: Block = null
  var blockAccelerator: Block = null
  var blockFulmination: Block = null
  var blockQuantumAssembler: Block = null
  var blockReactorCell: Block = null
  var blockUraniumOre: Block = null
  var blockToxicWaste: Block = null

  var itemCell: Item = null
  var itemFissileFuel: Item = null
  var itemBreedingRod: Item = null
  var itemDarkMatter: Item = null
  var itemAntimatter: Item = null
  var itemDeuteriumCell: Item = null
  var itemTritiumCell: Item = null
  var itemWaterCell: Item = null
  var itemBucketToxic: Item = null
  var itemYellowCake: Item = null
  var itemUranium: Item = null
  var itemHazmatTop: Item = null
  var itemHazmatBody: Item = null
  var itemHazmatLeggings: Item = null
  var itemHazmatBoots: Item = null
  val itemQuantumGlyph: Item = null

  /** Water, Uranium Hexafluoride, Steam, Deuterium, Toxic waste */
  var FLUIDSTACK_WATER: FluidStack = null
  var FLUIDSTACK_URANIUM_HEXAFLOURIDE: FluidStack = null
  var FLUIDSTACK_STEAM: FluidStack = null
  var FLUIDSTACK_DEUTERIUM: FluidStack = null
  var FLUIDSTACK_TRITIUM: FluidStack = null
  var FLUIDSTACK_TOXIC_WASTE: FluidStack = null


  var FLUID_URANIUM_HEXAFLOURIDE: Fluid = null
  var FLUID_PLASMA: Fluid = null
  var FLUID_STEAM: Fluid = null
  var FLUID_DEUTERIUM: Fluid = null
  var FLUID_TRITIUM: Fluid = null
  var FLUID_TOXIC_WASTE: Fluid = null


  var uraniumOreGeneration: OreGenerator = null

  override def init()
  {
    super.init()
  }

  override def postInit()
  {
    super.postInit()
    recipes += shaped(new ItemStack(itemAntimatter, 1, 1), itemAntimatter, itemAntimatter, itemAntimatter, itemAntimatter, itemAntimatter, AtomicContent.itemAntimatter, itemAntimatter, itemAntimatter)
    recipes += shaped(new ItemStack(itemAntimatter, 8, 0), new ItemStack(AtomicContent.itemAntimatter, 1, 1))


    recipes += shaped(new ItemStack(blockSteamFunnel, 2), " B ", "B B", "B B", 'B', UniversalRecipe.SECONDARY_METAL.get)
    recipes += shaped(new ItemStack(blockSteamFunnel, 2), " B ", "B B", "B B", 'B', "ingotIron")

    recipes += shaped(blockQuantumAssembler, "CCC", "SXS", "SSS", 'X', blockCentrifuge, 'C', UniversalRecipe.CIRCUIT_T3.get, 'S', UniversalRecipe.PRIMARY_PLATE.get)
    recipes += shaped(blockFulmination, "OSO", "SCS", "OSO", 'O', Blocks.obsidian, 'C', UniversalRecipe.CIRCUIT_T2.get, 'S', UniversalRecipe.PRIMARY_PLATE.get)
    recipes += shaped(blockAccelerator, "SCS", "CMC", "SCS", 'M', UniversalRecipe.MOTOR.get, 'C', UniversalRecipe.CIRCUIT_T3.get, 'S', UniversalRecipe.PRIMARY_PLATE.get)
    recipes += shaped(blockCentrifuge, "BSB", "MCM", "BSB", 'C', UniversalRecipe.CIRCUIT_T2.get, 'S', UniversalRecipe.PRIMARY_PLATE.get, 'B', UniversalRecipe.SECONDARY_METAL.get, 'M', UniversalRecipe.MOTOR.get)
    recipes += shaped(blockNuclearBoiler, "S S", "FBF", "SMS", 'F', Blocks.furnace, 'S', UniversalRecipe.PRIMARY_PLATE.get, 'B', Items.bucket, 'M', UniversalRecipe.MOTOR.get)
    recipes += shaped(blockChemicalExtractor, "BSB", "MCM", "BSB", 'C', UniversalRecipe.CIRCUIT_T3.get, 'S', UniversalRecipe.PRIMARY_PLATE.get, 'B', UniversalRecipe.SECONDARY_METAL.get, 'M', UniversalRecipe.MOTOR.get)

    recipes += shaped(new ItemStack(blockSiren, 2), "NPN", 'N', Blocks.noteblock, 'P', UniversalRecipe.SECONDARY_PLATE.get)
    recipes += shaped(blockReactorCell, "SCS", "MEM", "SCS", 'E', "cellEmpty", 'C', UniversalRecipe.CIRCUIT_T2.get, 'S', UniversalRecipe.PRIMARY_PLATE.get, 'M', UniversalRecipe.MOTOR.get)
    recipes += shaped(blockFusionCore, "CPC", "PFP", "CPC", 'P', UniversalRecipe.PRIMARY_PLATE.get, 'F', AtomicContent.blockReactorCell, 'C', UniversalRecipe.CIRCUIT_T3.get)
    recipes += shaped(new ItemStack(itemCell, 16), " T ", "TGT", " T ", 'T', "ingotTin", 'G', Blocks.glass)
    recipes += shaped(blockThermometer, "SSS", "GCG", "GSG", 'S', UniversalRecipe.PRIMARY_METAL.get, 'G', Blocks.glass, 'C', UniversalRecipe.CIRCUIT_T1.get)
    recipes += shaped(blockControlRod, "I", "I", "I", 'I', Items.iron_ingot)
    recipes += shaped(itemFissileFuel, "CUC", "CUC", "CUC", 'U', "ingotUranium", 'C', "cellEmpty")
    recipes += shaped(itemBreedingRod, "CUC", "CUC", "CUC", 'U', "breederUranium", 'C', "cellEmpty")

    //Hazmat recipes
    recipes += shaped(itemHazmatTop, "SSS", "BAB", "SCS", 'A', Items.leather_helmet, 'C', UniversalRecipe.CIRCUIT_T1.get, 'S', Blocks.wool)
    recipes += shaped(itemHazmatBody, "SSS", "BAB", "SCS", 'A', Items.leather_chestplate, 'C', UniversalRecipe.CIRCUIT_T1.get, 'S', Blocks.wool)
    recipes += shaped(itemHazmatLeggings, "SSS", "BAB", "SCS", 'A', Items.leather_leggings, 'C', UniversalRecipe.CIRCUIT_T1.get, 'S', Blocks.wool)
    recipes += shaped(itemHazmatBoots, "SSS", "BAB", "SCS", 'A', Items.leather_boots, 'C', UniversalRecipe.CIRCUIT_T1.get, 'S', Blocks.wool)

    //Electro magnets
    recipes += shaped(new ItemStack(blockElectromagnet, 2, 0), "BBB", "BMB", "BBB", 'B', UniversalRecipe.SECONDARY_METAL.get, 'M', UniversalRecipe.MOTOR.get)
    recipes += shapeless(new ItemStack(AtomicContent.blockElectromagnet, 1, 1), AtomicContent.blockElectromagnet, Blocks.glass)

    recipes += shapeless(new ItemStack(AtomicContent.itemWaterCell), "cellEmpty", Items.water_bucket)

  }
}

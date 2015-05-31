package com.calclavia.edx.core

/** @author Calclavia */
object Settings {
	//@Config(category = "Power", key = "AcceleratorCostPerTick")
	final val ACCELERATOR_ENERGY_COST_PER_TICK: Int = 4800000
	//@Config(key = "Tesla Sound FXs")
	var SOUND_FXS = true
	//Turbine Settings
	//@Config(key = "Shiny silver Wires")
	var SHINY_SILVER = true
	//@Config var allowTurbineStacking: Boolean = true
	// Power Settings
	//@Config(category = "Power")
	var fulminationOutputMultiplier: Double = 1
	//@Config(category = "Power", key = "WindTubineRatio")
	var WIND_POWER_RATIO: Int = 1
	//@Config(category = "Power", key = "WaterTubineRatio")
	var WATER_POWER_RATIO: Int = 1
	//@Config(category = "Power", key = "TubineRatio", comment = "Restricts the output of all turbines")
	var turbineOutputMultiplier: Double = 1

	//Disable/Enable Settings
	//@Config(category = "Enable")
	var allowToxicWaste: Boolean = true
	//@Config(category = "Enable")
	var allowRadioactiveOres: Boolean = true
	//@Config(category = "Enable", key = "EngineeringTableAutocraft")
	var ALLOW_ENGINEERING_AUTOCRAFT = true

	//Fluid Settings
	//@Config var fissionBoilVolumeMultiplier: Double = 1
	//@Config var uraniumHexaflourideRatio: Int = 200
	//@Config var waterPerDeutermium: Int = 4
	//@Config var deutermiumPerTritium: Int = 4
	//@Config var darkMatterSpawnChance: Double = 0.2
	//@Config var steamMultiplier: Double = 1

	//Recipe Settings
	//@Config var allowOreDictionaryCompatibility: Boolean = true
	//@Config var allowAlternateRecipes: Boolean = true
	//@Config(comment = "Put a list of block/item IDs to be used by the Quantum Assembler. Separate by commas, no space.")
	var quantumAssemblerRecipes: Array[String] = _
	//@Config(comment = "0 = Do not generate, 1 = Generate items only, 2 = Generate all")
	var quantumAssemblerGenerateMode: Int = 1
	//@Config var allowIC2UraniumCompression: Boolean = true
	//@Config
	var ACCELERATOR_ANITMATTER_DENSITY_MULTIPLIER: Int = 1

	/*
  @SubscribeEvent
  def configEvent(evt: ConfigEvent)
  {
    QuantumAssemblerRecipes.RECIPES.addAll(quantumAssemblerRecipes.map(x => new ItemStack(Block.blockRegistry.getObject(x).asInstanceOf[Block])).toList)
    PotionRadiation.INSTANCE.getId
  }*/
}
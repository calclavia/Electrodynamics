package resonantinduction.core

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraft.block.Block
import net.minecraft.item.ItemStack
import net.minecraftforge.common.config.Configuration
import resonant.api.recipe.QuantumAssemblerRecipes
import resonant.lib.config.Config
import resonant.lib.config.ConfigEvent.PostConfigEvent
import resonant.lib.prefab.poison.PotionRadiation

import scala.collection.convert.wrapAll._

/** @author Calclavia */
object Settings
{
    var config: Configuration = _

    @Config(key = "Tesla Sound FXs")
    var SOUND_FXS = true
    @Config(key = "Shiny silver Wires")
    var SHINY_SILVER = true

    //Turbine Settings
    @Config(category = "Power", key = "WindTubineRatio")
    var WIND_POWER_RATIO: Int = 1
    @Config(category = "Power", key = "WaterTubineRatio")
    var WATER_POWER_RATIO: Int = 1
    @Config var turbineOutputMultiplier: Double = 1
    @Config var allowTurbineStacking: Boolean = true

    // Power Settings
    @Config(category = "Power", key = "SolorPanel")
    var SOLAR_ENERGY: Int = 50
    @Config var fulminationOutputMultiplier: Double = 1

    //Disable/Enable Settings
    @Config var allowToxicWaste: Boolean = true
    @Config var allowRadioactiveOres: Boolean = true
    @Config(key = "EngineeringTableAutocraft")
    var ALLOW_ENGINEERING_AUTOCRAFT = true

    //Fluid Settings
    @Config var fissionBoilVolumeMultiplier: Double = 1
    @Config var uraniumHexaflourideRatio: Int = 200
    @Config var waterPerDeutermium: Int = 4
    @Config var deutermiumPerTritium: Int = 4
    @Config var darkMatterSpawnChance: Double = 0.2
    @Config var steamMultiplier: Double = 1

    //Recipe Settings
    @Config var allowOreDictionaryCompatibility: Boolean = true
    @Config var allowAlternateRecipes: Boolean = true
    @Config(comment = "Put a list of block/item IDs to be used by the Quantum Assembler. Separate by commas, no space.")
    var quantumAssemblerRecipes: Array[String] = _
    @Config(comment = "0 = Do not generate, 1 = Generate items only, 2 = Generate all")
    var quantumAssemblerGenerateMode: Int = 1
    @Config var allowIC2UraniumCompression: Boolean = true

    @SubscribeEvent
    def configEvent(evt: PostConfigEvent)
    {
        QuantumAssemblerRecipes.RECIPES.addAll(quantumAssemblerRecipes.map(x => new ItemStack(Block.blockRegistry.getObject(x).asInstanceOf[Block])).toList)
        PotionRadiation.INSTANCE.getId
    }
}
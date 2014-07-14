package resonantinduction.core

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraft.item.ItemStack
import net.minecraftforge.common.config.Configuration
import resonant.api.recipe.QuantumAssemblerRecipes
import resonant.lib.config.Config
import resonant.lib.config.ConfigEvent.PostConfigEvent
import resonant.lib.prefab.poison.PotionRadiation

/** @author Calclavia */
object Settings
{
  var config: Configuration = _

  @Config(key = "Engineering Table Autocraft")
  var ALLOW_ENGINEERING_AUTOCRAFT = true
  @Config(key = "Tesla Sound FXs")
  var SOUND_FXS = true
  @Config(key = "Shiny silver Wires")
  var SHINY_SILVER = true
  @Config(key = "Max EM Contractor Path")
  var MAX_LEVITATOR_DISTANCE: Int = 200
  @Config(category = Configuration.CATEGORY_GENERAL, key = "Levitator Max Reach")
  var LEVITATOR_MAX_REACH: Int = 40
  @Config(category = Configuration.CATEGORY_GENERAL, key = "Levitator Push Delay")
  var LEVITATOR_PUSH_DELAY: Int = 5
  @Config(category = Configuration.CATEGORY_GENERAL, key = "Levitator Max Speed")
  var LEVITATOR_MAX_SPEED: Double = .2
  @Config(category = Configuration.CATEGORY_GENERAL, key = "Levitator Acceleration")
  var LEVITATOR_ACCELERATION: Double = .02
  @Config(category = "Power", key = "Wind_tubine_Ratio")
  var WIND_POWER_RATIO: Int = 1
  @Config(category = "Power", key = "Water_tubine_Ratio")
  var WATER_POWER_RATIO: Int = 1
  @Config(category = "Power", key = "Solor_Panel")
  var SOLAR_ENERGY: Int = 50
  @Config var fulminationOutputMultiplier: Double = 1
  @Config var turbineOutputMultiplier: Double = 1
  @Config var fissionBoilVolumeMultiplier: Double = 1
  @Config var allowTurbineStacking: Boolean = true
  @Config var allowToxicWaste: Boolean = true
  @Config var allowRadioactiveOres: Boolean = true
  @Config var allowOreDictionaryCompatibility: Boolean = true
  @Config var allowAlternateRecipes: Boolean = true
  @Config var allowIC2UraniumCompression: Boolean = true
  @Config(comment = "0 = Do not generate, 1 = Generate items only, 2 = Generate all")
  var quantumAssemblerGenerateMode: Int = 1
  @Config var uraniumHexaflourideRatio: Int = 200
  @Config var waterPerDeutermium: Int = 4
  @Config var deutermiumPerTritium: Int = 4
  @Config(comment = "Put a list of block/item IDs to be used by the Quantum Assembler. Separate by commas, no space.")
  var quantumAssemblerRecipes: Array[Int] = new Array[Int](0)
  @Config var darkMatterSpawnChance: Double = 0.2

  @SubscribeEvent
  def configEvent(evt: PostConfigEvent)
  {
    for (recipeID <- quantumAssemblerRecipes)
    {
      try
      {
        QuantumAssemblerRecipes.addRecipe(new ItemStack(recipeID, 1, 0))
      }
      catch
        {
          case e: Exception =>
          {
            e.printStackTrace
          }
        }
    }
    PotionRadiation.INSTANCE.getId
  }
}
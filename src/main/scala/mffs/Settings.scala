package mffs

import net.minecraftforge.common.config.Configuration
import resonant.lib.config.Config

/**
 * MFFS Configuration Settings
 *
 * @author Calclavia
 */
object Settings
{
  var config: Configuration = _
  final val maxFrequencyDigits: Int = 6

  @Config
  var maxForceFieldsPerTick: Int = 1000
  @Config
  var maxForceFieldScale: Int = 200
  @Config
  var fortronProductionMultiplier: Double = 1
  @Config(comment = "Should the interdiction matrix interact with creative players?.")
  var interdictionInteractCreative: Boolean = true
  @Config(comment = "Set this to false to turn off the MFFS Chunkloading capabilities.")
  var loadFieldChunks: Boolean = true
  @Config(comment = "Allow the operator(s) to override security measures created by MFFS?")
  var allowOpOverride: Boolean = true
  @Config(comment = "Cache allows temporary data saving to decrease calculations required.")
  var useCache: Boolean = true
  @Config(comment = "Turning this to false will make MFFS run without electricity or energy systems required. Great for vanilla!")
  var enableElectricity: Boolean = true
  @Config(comment = "Turning this to false will enable better client side packet and updates but in the cost of more packets sent.")
  var conservePackets: Boolean = true
  @Config(comment = "Turning this to false will reduce rendering and client side packet graphical packets.")
  var highGraphics: Boolean = true
  @Config(comment = "The energy required to perform a kill for the interdiction matrix.")
  var interdictionMatrixMurderEnergy: Int = 0
  @Config(comment = "The maximum range for the interdiction matrix.")
  var interdictionMatrixMaxRange: Int = Integer.MAX_VALUE
  @Config
  var enableForceManipulator: Boolean = true
  @Config
  var allowForceManipulatorTeleport: Boolean = true
  @Config
  var allowFortronTeleport: Boolean = true
  @Config(comment = "A list of block names to not be moved by the force mobilizer.")
  var mobilizerBlacklist: Array[String] = _
  @Config(comment = "A list of block names to not be stabilized by the electromagnetic projector.")
  var stabilizationBlacklist: Array[String] = _
  @Config(comment = "A list of block names to not be disintegrated by the electromagnetic projector.")
  var disintegrationBlacklist: Array[String] = _
}
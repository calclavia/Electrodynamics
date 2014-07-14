package resonantinduction.core

import java.util.logging.Logger

/**
 * A class for global references.
 *
 * @author Calclavia
 */
object Reference
{
  final val idPrefix = "ResonantInduction"

  /** The official name of the mod */
  final val NAME: String = "Resonant Induction"
  final val LOGGER = Logger.getLogger(Reference.NAME)

  final val MAJOR_VERSION: String = "@MAJOR@"
  final val MINOR_VERSION: String = "@MINOR@"
  final val REVISION_VERSION: String = "@REVIS@"
  final val BUILD_VERSION: String = "@BUILD@"
  final val VERSION: String = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION
  final val CHANNEL: String = "resonindc"
  /**
   * Directory Information
   */
  final val DOMAIN: String = "resonantinduction"
  final val PREFIX: String = DOMAIN + ":"
  final val DIRECTORY: String = "/assets/" + DOMAIN + "/"
  final val TEXTURE_DIRECTORY: String = "textures/"
  final val GUI_DIRECTORY: String = TEXTURE_DIRECTORY + "gui/"
  final val BLOCK_TEXTURE_DIRECTORY: String = TEXTURE_DIRECTORY + "blocks/"
  final val ITEM_TEXTURE_DIRECTORY: String = TEXTURE_DIRECTORY + "items/"
  final val MODEL_PATH: String = "models/"
  final val MODEL_DIRECTORY: String = DIRECTORY + MODEL_PATH
}
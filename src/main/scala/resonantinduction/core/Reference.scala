package resonantinduction.core

import java.util.logging.Logger

/**
 * A class for global references.
 *
 * @author Calclavia
 */
final object Reference
{
  final val idPrefix = "ResonantInduction"
  final val coreID = idPrefix + ":Core"

  /** The official name of the mod */
  final val name = "Resonant Induction"
  final val logger = Logger.getLogger(Reference.name)

  final val majorVersion = "@MAJOR@"
  final val minorVersion = "@MINOR@"
  final val revisionVersion = "@REVIS@"
  final val build = "@BUILD@"
  final val version = majorVersion + "." + minorVersion + "." + revisionVersion
  final val channel: String = "resonindc"
  /**
   * Directory Information
   */
  final val domain: String = "resonantinduction"
  final val prefix: String = domain + ":"
  final val assetDirectory: String = "/assets/" + domain + "/"
  final val textureDirectory: String = "textures/"
  final val guiDirectory: String = textureDirectory + "gui/"
  final val blockTextureDirectory: String = textureDirectory + "blocks/"
  final val itemTextureDirectory: String = textureDirectory + "items/"
  final val modelPath: String = "models/"
  final val modelDirectory: String = assetDirectory + modelPath
  final val FX_DIRECTORY = textureDirectory + "fx/"
}
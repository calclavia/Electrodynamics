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
  final val name: String = "Resonant Induction"
  final val logger = Logger.getLogger(Reference.name)

  final val majorVersion: String = "@MAJOR@"
  final val minorVersion: String = "@MINOR@"
  final val revisionVersion: String = "@REVIS@"
  final val build: String = "@BUILD@"
  final val version: String = majorVersion + "." + minorVersion + "." + revisionVersion
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
}
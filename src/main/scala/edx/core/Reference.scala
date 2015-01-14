package edx.core

import java.util.logging.Logger

/**
 * A class for global references.
 *
 * @author Calclavia
 */
object Reference
{
  final val id = "EDX"

  /** The official name of the mod */
  final val name = "Electrodynamics"
  final val logger = Logger.getLogger(Reference.name)

  final val majorVersion = "@MAJOR@"
  final val minorVersion = "@MINOR@"
  final val revisionVersion = "@REVIS@"
  final val build = "@BUILD@"
  final val version = majorVersion + "." + minorVersion + "." + revisionVersion
  /**
   * Directory Information
   */
  final val domain: String = "edx"
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
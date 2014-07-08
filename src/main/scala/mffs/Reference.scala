package mffs

import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.LogManager

/**
 * A static variable reference file.
 * @author Calclavia
 */
final object Reference
{
  /**
   * General Variable Definition
   */
  final val channel = "MFFS"
  final val id = "MFFS"
  final val name = "Modular Force Field System"
  final val logger = LogManager.getLogger(name)
  final val domain = "mffs"
  final val prefix = domain + ":"
  final val majorVersion = "@MAJOR@"
  final val minorVersion = "@MINOR@"
  final val revisionVersion = "@REVIS@"
  final val version = majorVersion + "." + minorVersion + "." + revisionVersion
  final val buildVersion = "@BUILD@"

  /**
   * Directories Definition
   */
  final val resourceDirectory = "/assets/mffs/"
  final val textureDirectory = "textures/"
  final val blockDirectory = textureDirectory + "blocks/"
  final val itemDirectory = textureDirectory + "items/"
  final val modelDirectory = resourceDirectory + "models/"
  final val guiDirectory = textureDirectory + "gui/"

  final val hologramTexture = new ResourceLocation(domain, modelDirectory + "hologram.png")
  final val guiButtonTexture = new ResourceLocation(domain, guiDirectory + "gui_button.png")
}

package mffs

import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.LogManager

/**
 * A static variable reference file.
 * @author Calclavia
 */
object Reference
{
  /**
   * General Variable Definition
   */
  final val CHANNEL = "MFFS"
  final val ID = "MFFS"
  final val NAME = "Modular Force Field System"
  final val LOGGER = LogManager.getLogger(NAME)
  final val DOMAIN = "mffs"
  final val PREFIX = DOMAIN + ":"
  final val MAJOR_VERSION = "@MAJOR@"
  final val MINOR_VERSION = "@MINOR@"
  final val REVISION_VERSION = "@REVIS@"
  final val VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION
  final val BUILD_VERSION = "@BUILD@"

  /**
   * Directories Definition
   */
  final val RESOURCE_DIRECTORY: String = "/assets/mffs/"
  final val TEXTURE_DIRECTORY: String = "textures/"
  final val BLOCK_DIRECTORY: String = TEXTURE_DIRECTORY + "blocks/"
  final val HOLOGAM_TEXTURE: ResourceLocation = new ResourceLocation(DOMAIN, BLOCK_DIRECTORY + "forceField.png")
  final val ITEM_DIRECTORY: String = TEXTURE_DIRECTORY + "items/"
  final val MODEL_DIRECTORY: String = TEXTURE_DIRECTORY + "models/"
  final val GUI_DIRECTORY: String = TEXTURE_DIRECTORY + "gui/"
  final val GUI_BUTTON: ResourceLocation = new ResourceLocation(DOMAIN, GUI_DIRECTORY + "gui_button.png")
}

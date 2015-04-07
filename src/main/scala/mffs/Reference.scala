package mffs

/**
 * A static variable reference file.
 * @author Calclavia
 */
final object Reference {
	/**
	 * General Variable Definition
	 */
	final val channel = "MFFS"
	final val id = "mffs"
	final val name = "Modular Force Field System"
	final val domain = id
	final val prefix = domain + ":"
	final val majorVersion = "@MAJOR@"
	final val minorVersion = "@MINOR@"
	final val revisionVersion = "@REVIS@"
	final val buildVersion = "@BUILD@"
	final val version = majorVersion + "." + minorVersion + "." + revisionVersion

	/**
	 * Directories Definition
	 */
	final val resourceDirectory = "/assets/mffs/"
	final val textureDirectory = "textures/"
	final val blockDirectory = textureDirectory + "blocks/"
	final val itemDirectory = textureDirectory + "items/"
	final val modelPath = "models/"
	final val modelDirectory = resourceDirectory + "models/"
	final val guiDirectory = textureDirectory + "gui/"
}

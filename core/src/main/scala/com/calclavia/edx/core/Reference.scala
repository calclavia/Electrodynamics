package com.calclavia.edx.core

import java.util.logging.Logger

/**
 * A class for global references.
 *
 * @author Calclavia
 */
object Reference
{
  final val id = "edx"

	final val mechanicID = id + ":mechanic"
	final val electricID = id + ":electric"
	final val quantumID = id + ":quantum"

  /** The official name of the mod */
  final val name = "Electrodynamics"
  final val logger = Logger.getLogger(Reference.name)

  final val majorVersion = "@MAJOR@"
  final val minorVersion = "@MINOR@"
  final val revisionVersion = "@REVIS@"
  final val build = "@BUILD@"
  final val version = majorVersion + "." + minorVersion + "." + revisionVersion

	final val novaVersion = "0.0.1"

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

	/**
	 *
	MultipartGenerator.registerTrait("resonantengine.api.graph.INodeProvider", "edx.core.prefab.pass.TraitNodeProvider")
		MultipartGenerator.registerTrait("resonantengine.api.tile.IDebugInfo", "edx.core.prefab.pass.TraitDebugInfo")
		MultipartGenerator.registerPassThroughInterface("net.minecraftforge.fluids.IFluidHandler")
	 */
}
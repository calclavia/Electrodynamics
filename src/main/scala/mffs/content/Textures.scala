package mffs.content

import com.resonant.core.prefab.modcontent.ContentLoader
import mffs.Reference
import nova.core.render.texture.BlockTexture

/**
 * Textures
 * @author Calclavia
 */
object Textures extends ContentLoader {
	val machine = new BlockTexture(Reference.domain, "machine")
	val fortron = new BlockTexture(Reference.domain, "fortron")
	val hologram = new BlockTexture(Reference.domain, "hologram.png")
	val forceField = new BlockTexture(Reference.domain, "forceField.png")

	val fortronCapacitorOn = new BlockTexture(Reference.domain, "fortronCapacitor_on.png")
	val fortronCapacitorOff = new BlockTexture(Reference.domain, "fortronCapacitor_off.png")

	val projectorOn = new BlockTexture(Reference.domain, "electromagneticProjector_on.png")
	val projectorOff = new BlockTexture(Reference.domain, "electromagneticProjector_off.png")

	val biometricOn = new BlockTexture(Reference.domain, "biometricIdentifier_on.png")
	val biometricOff = new BlockTexture(Reference.domain, "biometricIdentifier_off.png")

}

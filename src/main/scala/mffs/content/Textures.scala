package mffs.content

import com.resonant.core.prefab.modcontent.ContentLoader
import mffs.Reference
import nova.core.render.texture.BlockTexture

/**
 * Textures
 * @author Calclavia
 */
object Textures extends ContentLoader {
	val machine = new BlockTexture(Reference.id, "machine")
	val fortron = new BlockTexture(Reference.id, "fortron")

	val fortronCapacitorOn = new BlockTexture(Reference.domain, "fortronCapacitor_on.png")
	val fortronCapacitorOff = new BlockTexture(Reference.domain, "fortronCapacitor_off.png")
}

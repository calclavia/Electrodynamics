package mffs

import com.resonant.core.prefab.modcontent.ContentLoader
import nova.core.render.texture.BlockTexture

/**
 * Textures
 * @author Calclavia
 */
object Texture extends ContentLoader {
	val machine = new BlockTexture(Reference.id, "machine")
	val fortron = new BlockTexture(Reference.id, "fortron")

}
